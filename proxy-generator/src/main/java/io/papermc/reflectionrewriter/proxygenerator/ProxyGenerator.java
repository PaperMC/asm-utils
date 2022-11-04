package io.papermc.reflectionrewriter.proxygenerator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V17;

@DefaultQualifier(NonNull.class)
public final class ProxyGenerator {
    /*
    public static void main(String[] args) throws Exception {
        final File file = new File("test.class");
        final byte[] generated = generateProxy(Test.class, "io/papermc/paper/Test123");
        file.getAbsoluteFile().getParentFile().mkdirs();
        Files.write(file.toPath(), generated);
    }
     */

    private ProxyGenerator() {
    }

    public static byte[] generateProxy(final byte[] proxyImplementation, final String generatedClassName) throws IOException {
        return generateProxy(new ClassReader(proxyImplementation), generatedClassName);
    }

    public static byte[] generateProxy(final Class<?> proxyImplementation, final String generatedClassName) throws IOException {
        return generateProxy(classReader(proxyImplementation.getName()), generatedClassName);
    }

    public static byte[] generateProxy(final ClassReader reader, final String generatedClassName) throws IOException {
        // Discover methods we need to generate static proxies for

        // First, scan provided impl class for non-static public methods and impl name
        final DiscoverMethodsVisitor scanImpl = new DiscoverMethodsVisitor(Opcodes.ASM9, null);
        reader.accept(scanImpl, 0);
        final Set<MethodInfo> methods = new HashSet<>(scanImpl.methods);
        final String proxy = scanImpl.name;

        // Next, scan the ReflectionProxy interface
        // This allows indirect implementation to work (i.e. Impl extends AbstractBase; AbstractBase implements ReflectionProxy)
        // as well as default methods
        final ClassReader interfaceReader = classReader(ReflectionProxy.class.getName());
        final DiscoverMethodsVisitor scanInterface = new DiscoverMethodsVisitor(Opcodes.ASM9, null);
        interfaceReader.accept(scanInterface, 0);
        methods.addAll(scanInterface.methods);

        // Generate our proxy
        final ClassWriter classWriter = new ClassWriter(0);

        // Header
        classWriter.visit(V17, ACC_PUBLIC | ACC_FINAL | ACC_SUPER, generatedClassName, null, "java/lang/Object", null);
        // INSTANCE field
        instanceField(classWriter, proxy);
        // Private default constructor
        constructor(classWriter);
        // public static void init(proxyInstance)
        initMethod(classWriter, generatedClassName, proxy);
        // proxy methods
        for (final MethodInfo method : methods) {
            proxyMethod(classWriter, generatedClassName, proxy, method);
        }
        // done
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private static void instanceField(final ClassWriter classWriter, final String proxy) {
        final FieldVisitor fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_STATIC, "INSTANCE", "L" + proxy + ";", null, null);
        fieldVisitor.visitEnd();
    }

    private static void constructor(final ClassWriter classWriter) {
        final MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        methodVisitor.visitTypeInsn(NEW, "java/lang/IllegalStateException");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "()V", false);
        methodVisitor.visitInsn(ATHROW);
        methodVisitor.visitMaxs(2, 1);
        methodVisitor.visitEnd();
    }

    private static void initMethod(final ClassWriter classWriter, final String generatedClassName, final String proxy) {
        final MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "init", "(L" + proxy + ";)V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(PUTSTATIC, generatedClassName, "INSTANCE", "L" + proxy + ";");
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    private static void proxyMethod(final ClassWriter classWriter, final String generatedClassName, final String proxy, final MethodInfo method) {
        final MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, method.name, method.descriptor, method.signature, method.exceptions);
        visitor.visitCode();
        visitor.visitFieldInsn(GETSTATIC, generatedClassName, "INSTANCE", "L" + proxy + ";");
        final Type methodType = Type.getType(method.descriptor);
        int locals = 0;
        for (final Type argumentType : methodType.getArgumentTypes()) {
            visitor.visitVarInsn(argumentType.getOpcode(ILOAD), locals);
            locals++;
        }
        visitor.visitMethodInsn(INVOKEVIRTUAL, proxy, method.name, method.descriptor, false);
        visitor.visitInsn(methodType.getReturnType().getOpcode(IRETURN));
        visitor.visitMaxs(locals + 1, locals);
        visitor.visitEnd();
    }

    private static ClassReader classReader(final String className) {
        try (final @Nullable InputStream is = ProxyGenerator.class.getClassLoader().getResourceAsStream(className.replace('.', '/') + ".class")) {
            Objects.requireNonNull(is);
            return new ClassReader(is);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private record MethodInfo(
        String name,
        String descriptor,
        @Nullable String signature,
        String @Nullable [] exceptions
    ) {
        // need to override equals and hashcode because of useless array default

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final MethodInfo that = (MethodInfo) o;
            return this.name.equals(that.name)
                && this.descriptor.equals(that.descriptor)
                && Objects.equals(this.signature, that.signature)
                && Arrays.equals(this.exceptions, that.exceptions);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(this.name, this.descriptor, this.signature);
            result = 31 * result + Arrays.hashCode(this.exceptions);
            return result;
        }
    }

    private static final class DiscoverMethodsVisitor extends ClassVisitor {
        private final List<MethodInfo> methods;
        private @MonotonicNonNull String name;

        DiscoverMethodsVisitor(final int api, final @Nullable ClassVisitor visitor) {
            super(api, visitor);
            this.methods = new ArrayList<>();
        }

        @Override
        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            this.name = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
            if (!Modifier.isStatic(access) && Modifier.isPublic(access) && !name.equals("<init>")) {
                this.methods.add(new MethodInfo(name, descriptor, signature, exceptions));
            }
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
    }
}

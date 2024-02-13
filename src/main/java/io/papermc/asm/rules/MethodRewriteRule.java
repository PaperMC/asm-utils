package io.papermc.asm.rules;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

import static io.papermc.asm.util.DescriptorUtils.fromExecutable;
import static io.papermc.asm.util.DescriptorUtils.parseMethod;

public interface MethodRewriteRule extends OwnableRewriteRule {

    String LAMBDA_METAFACTORY_OWNER = "java/lang/invoke/LambdaMetafactory";

    private static String transformExecutableName(final Executable executable) {
        return executable instanceof Constructor<?> ? "<init>" : executable.getName();
    }

    MethodMatcher methodMatcher();

    @Override
    default ClassVisitor createVisitor(final int api, final ClassVisitor parent, final ClassProcessingContext context) {
        return new ClassVisitor(api, parent) {

            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
                final MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                final MethodNode mn = new MethodNode(this.api, access, name, descriptor, signature, exceptions);
                return new MethodVisitor(this.api, mn) {
                    @Override
                    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
                        if (MethodRewriteRule.this.matchesOwner(context, owner) && MethodRewriteRule.this.methodMatcher().matches(name, descriptor)) {
                            final @Nullable Rewrite rewrite = MethodRewriteRule.this.rewrite(context, false, opcode, owner, name, parseMethod(descriptor), isInterface);
                            if (rewrite != null) {
                                rewrite.apply(this.getDelegate(), mn);
                                return;
                            }
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }

                    @Override
                    public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
                        if (LAMBDA_METAFACTORY_OWNER.equals(bootstrapMethodHandle.getOwner()) && bootstrapMethodArguments.length > 1 && bootstrapMethodArguments[1] instanceof final Handle handle) {
                            if (MethodRewriteRule.this.matchesOwner(context, handle.getOwner()) && MethodRewriteRule.this.methodMatcher().matches(handle.getName(), handle.getDesc())) {
                                final @Nullable Rewrite rewrite = MethodRewriteRule.this.rewrite(context, true, handle.getTag(), handle.getOwner(), handle.getName(), parseMethod(handle.getDesc()), handle.isInterface());
                                if (rewrite != null) {
                                    bootstrapMethodArguments[1] = rewrite.createHandle();
                                }
                            }
                        }
                        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                    }

                    @Override
                    public void visitEnd() {
                        mn.accept(methodVisitor); // write possibly modified MethodNode
                        super.visitEnd();
                    }
                };
            }

        };
    }

    default Stream<Map.Entry<Executable, MethodTypeDesc>> matchingMethodsByName() {
        return this.owners().stream()
            .flatMap(o -> Stream.concat(Arrays.stream(o.getDeclaredMethods()), Arrays.stream(o.getDeclaredConstructors())))
            .filter(executable -> Modifier.isPublic(executable.getModifiers())) // we only care about public stuff since that is API
            .map(executable -> Map.entry(executable, fromExecutable(executable)))
            .filter(pair -> this.methodMatcher().matchesName(transformExecutableName(pair.getKey())));
    }

    @Nullable Rewrite rewrite(ClassProcessingContext context, boolean invokeDynamic, int opcode, String owner, String name, MethodTypeDesc descriptor, boolean isInterface);

    interface Rewrite {

        void apply(MethodVisitor delegate, MethodNode context);

        Handle createHandle();
    }

    record RewriteSingle(int opcode, String owner, String name, MethodTypeDesc descriptor, boolean isInterface) implements Rewrite {

        @Override
        public void apply(final MethodVisitor delegate, final MethodNode context) {
            delegate.visitMethodInsn(this.opcode(), this.owner(), this.name(), this.descriptor().descriptorString(), this.isInterface());
        }

        @Override
        public Handle createHandle() {
            return new Handle(this.opcode(), this.owner(), this.name(), this.descriptor().descriptorString(), this.isInterface());
        }
    }

}

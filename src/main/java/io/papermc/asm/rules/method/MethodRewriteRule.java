package io.papermc.asm.rules.method;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.generate.GeneratedMethodHolder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;

import static io.papermc.asm.util.DescriptorUtils.fromOwner;
import static io.papermc.asm.util.DescriptorUtils.methodDesc;
import static io.papermc.asm.util.DescriptorUtils.toOwner;

public interface MethodRewriteRule extends RewriteRule {

    String LAMBDA_METAFACTORY_OWNER = "java/lang/invoke/LambdaMetafactory";

    /**
     * Checks if this rule should even attempt to create a {@link Rewrite} for the current context.
     * Returning true here does <b>not</b> mean a {@link Rewrite} will be created, but it's an early exit
     * for rules that know they won't be able to rewrite the method.
     *
     * @param context the current context
     * @param opcode the method opcode
     * @param owner the method owner (with slashes)
     * @param name the method name
     * @param descriptor the method descriptor
     * @param isInterface if the owning class is an interface
     * @return true to continue processing the instruction
     */
    default boolean shouldProcess(final ClassProcessingContext context, final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        return true;
    }

    @Override
    default ClassVisitor createVisitor(final int api, final ClassVisitor parent, final ClassProcessingContext context) {
        record MethodKey(String owner, String name, MethodTypeDesc descriptor) {
        }
        final Map<MethodKey, MethodGenerator> methodsToGenerate = new HashMap<>();
        return new ClassVisitor(api, parent) {

            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
                final MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                final MethodNode mn = new MethodNode(this.api, access, name, descriptor, signature, exceptions);
                return new MethodVisitor(this.api, mn) {
                    @Override
                    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
                        if (MethodRewriteRule.this.shouldProcess(context, opcode, owner, name, descriptor, isInterface)) {
                            final ClassDesc methodOwner = fromOwner(owner);
                            final MethodTypeDesc methodDesc = methodDesc(descriptor);
                            final @Nullable Rewrite rewrite = MethodRewriteRule.this.rewrite(context, false, opcode, methodOwner, name, methodDesc, isInterface);
                            if (rewrite != null) {
                                rewrite.apply(this.getDelegate(), mn);
                                final @Nullable MethodGenerator willGenerate = rewrite.createFactory();
                                if (willGenerate != null) {
                                    methodsToGenerate.put(new MethodKey(owner, name, methodDesc), willGenerate);
                                }
                                return;
                            }
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }

                    @Override
                    public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
                        if (LAMBDA_METAFACTORY_OWNER.equals(bootstrapMethodHandle.getOwner()) && bootstrapMethodArguments.length > 1 && bootstrapMethodArguments[1] instanceof final Handle handle) {
                            if (MethodRewriteRule.this.shouldProcess(context, handle.getTag(), handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface())) {
                                final ClassDesc handleOwner = fromOwner(handle.getOwner());
                                final MethodTypeDesc handleDesc = methodDesc(handle.getDesc());
                                final @Nullable Rewrite rewrite = MethodRewriteRule.this.rewrite(context, true, handle.getTag(), handleOwner, handle.getName(), handleDesc, handle.isInterface());
                                if (rewrite != null) {
                                    bootstrapMethodArguments[1] = rewrite.createHandle();
                                    final @Nullable MethodGenerator willGenerate = rewrite.createFactory();
                                    if (willGenerate != null) {
                                        methodsToGenerate.put(new MethodKey(handle.getOwner(), handle.getName(), handleDesc), willGenerate);
                                    }
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

            @Override
            public void visitEnd() {
                final GeneratorAdapterFactory factory = (access, name, descriptor) -> {
                    final MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, null, null);
                    return new GeneratorAdapter(methodVisitor, access, name, descriptor);
                };
                for (final MethodGenerator consumer : methodsToGenerate.values()) {
                    consumer.generate(factory);
                }
                super.visitEnd();
            }
        };
    }

    @Nullable Rewrite rewrite(ClassProcessingContext context, boolean isInvokeDynamic, int opcode, ClassDesc owner, String name, MethodTypeDesc descriptor, boolean isInterface);

    interface Rewrite {

        void apply(MethodVisitor delegate, MethodNode context);

        Handle createHandle();

        @Nullable MethodGeneratorFactory methodGeneratorFactory();

        default @Nullable MethodGenerator createFactory() {
            final @Nullable MethodGeneratorFactory factoryFactory = this.methodGeneratorFactory();
            if (factoryFactory != null) {
                return factory -> factoryFactory.create(this.createModifiedData()).generate(factory);
            }
            return null;
        }

        GeneratedMethodHolder.MethodCallData createModifiedData();

        Rewrite withFactory(MethodGeneratorFactory factoryFactory);
    }

    @FunctionalInterface
    interface MethodGeneratorFactory {

        MethodGenerator create(GeneratedMethodHolder.MethodCallData modifiedCall);
    }

    @FunctionalInterface
    interface MethodGenerator {

        void generate(GeneratorAdapterFactory factory);
    }

    record RewriteSingle(int opcode, ClassDesc owner, String name, MethodTypeDesc descriptor, boolean isInterface, boolean isInvokeDynamic, @Nullable MethodGeneratorFactory methodGeneratorFactory) implements Rewrite {

        public RewriteSingle(final int opcode, final ClassDesc owner, final String name, final MethodTypeDesc descriptor, final boolean isInterface, final boolean isInvokeDynamic) {
            this(opcode, owner, name, descriptor, isInterface, isInvokeDynamic, null);
        }

        @Override
        public void apply(final MethodVisitor delegate, final MethodNode context) {
            delegate.visitMethodInsn(this.opcode(), toOwner(this.owner()), this.name(), this.descriptor().descriptorString(), this.isInterface());
        }

        @Override
        public Handle createHandle() {
            return new Handle(this.opcode(), toOwner(this.owner()), this.name(), this.descriptor().descriptorString(), this.isInterface());
        }

        @Override
        public GeneratedMethodHolder.MethodCallData createModifiedData() {
            return new GeneratedMethodHolder.MethodCallData(Opcodes.INVOKESTATIC, this.owner(), this.name(), this.descriptor(), this.isInvokeDynamic());
        }

        @Override
        public Rewrite withFactory(final MethodGeneratorFactory factoryFactory) {
            return new RewriteSingle(this.opcode(), this.owner(), this.name(), this.descriptor(), this.isInterface(), this.isInvokeDynamic(), factoryFactory);
        }
    }

}

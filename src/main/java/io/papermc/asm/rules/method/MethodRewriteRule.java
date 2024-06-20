package io.papermc.asm.rules.method;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.generate.GeneratedMethodHolder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.LinkedHashMap;
import java.util.Map;
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
        final Map<MethodKey, MethodGenerator> methodsToGenerate = new LinkedHashMap<>();
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
                            final @Nullable Rewrite<?> rewrite = MethodRewriteRule.this.rewrite(context, false, opcode, methodOwner, name, methodDesc, isInterface);
                            if (rewrite != null) {
                                rewrite.apply(this.getDelegate(), mn);
                                final @Nullable MethodGenerator willGenerate = rewrite.createMethodGenerator();
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
                                final @Nullable Rewrite<?> rewrite = MethodRewriteRule.this.rewrite(context, true, handle.getTag(), handleOwner, handle.getName(), handleDesc, handle.isInterface());
                                if (rewrite != null) {
                                    bootstrapMethodArguments[1] = rewrite.createHandle();
                                    final @Nullable MethodGenerator willGenerate = rewrite.createMethodGenerator();
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

    @Nullable Rewrite<?> rewrite(ClassProcessingContext context, boolean isInvokeDynamic, int opcode, ClassDesc owner, String name, MethodTypeDesc descriptor, boolean isInterface);

    interface Rewrite<D extends GeneratedMethodHolder.CallData> {

        void apply(MethodVisitor delegate, MethodNode context);

        Handle createHandle();

        Rewrite<D> withGeneratorInfo(GeneratedMethodHolder holder, D original);

        Rewrite<D> withNamePrefix(String prefix);

        @Nullable MethodGenerator createMethodGenerator();
    }

    record GeneratorInfo<D extends GeneratedMethodHolder.CallData>(GeneratedMethodHolder holder, D original) {
    }

    @FunctionalInterface
    interface MethodGenerator {

        void generate(GeneratorAdapterFactory factory);
    }

    /**
     * Holds the structure of the rewritten method that replaces a matching method found in the bytecode.
     *
     * @param opcode the replaced opcode
     * @param owner the replaced owner
     * @param name the replaced name
     * @param descriptor the replaced descriptor
     * @param isInterface if the replaced method is an interface method
     * @param isInvokeDynamic if the replaced method is an invokedynamic
     * @param generatorInfo info for generating the method (optional)
     */
    record RewriteSingle(int opcode, ClassDesc owner, String name, MethodTypeDesc descriptor, boolean isInterface, boolean isInvokeDynamic, @Nullable GeneratorInfo<GeneratedMethodHolder.MethodCallData> generatorInfo) implements Rewrite<GeneratedMethodHolder.MethodCallData> {

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
        public Rewrite<GeneratedMethodHolder.MethodCallData> withNamePrefix(final String prefix) {
            return new RewriteSingle(this.opcode(), this.owner(), prefix + this.name(), this.descriptor(), this.isInterface(), this.isInvokeDynamic(), this.generatorInfo());
        }

        @Override
        public Rewrite<GeneratedMethodHolder.MethodCallData> withGeneratorInfo(final GeneratedMethodHolder holder, final GeneratedMethodHolder.MethodCallData original) {
            return new RewriteSingle(this.opcode(), this.owner(), this.name(), this.descriptor(), this.isInterface(), this.isInvokeDynamic(), new GeneratorInfo<>(holder, original));
        }

        @Override
        public @Nullable MethodGenerator createMethodGenerator() {
            if (this.generatorInfo == null) {
                return null;
            }
            final GeneratedMethodHolder.MethodCallData original = this.generatorInfo.original();
            return factory -> {
                this.generatorInfo.holder().generateMethod(
                    factory,
                    new GeneratedMethodHolder.MethodCallData(Opcodes.INVOKESTATIC, this.owner(), this.name(), this.descriptor(), this.isInvokeDynamic()),
                    original
                );
            };
        }
    }
}

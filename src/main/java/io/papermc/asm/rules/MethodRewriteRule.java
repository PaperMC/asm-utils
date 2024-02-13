package io.papermc.asm.rules;

import io.papermc.asm.ClassProcessingContext;
import java.lang.constant.MethodTypeDesc;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

import static io.papermc.asm.util.DescriptorUtils.parseMethod;

public interface MethodRewriteRule extends RewriteRule {

    String LAMBDA_METAFACTORY_OWNER = "java/lang/invoke/LambdaMetafactory";

    default boolean shouldProcess(final ClassProcessingContext context, final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        return true;
    }

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
                        if (MethodRewriteRule.this.shouldProcess(context, opcode, owner, name, descriptor, isInterface)) {
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
                            if (MethodRewriteRule.this.shouldProcess(context, handle.getTag(), handle.getOwner(), name, descriptor, handle.isInterface())) {
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

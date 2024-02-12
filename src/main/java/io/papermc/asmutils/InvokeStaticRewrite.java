package io.papermc.asmutils;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@DefaultQualifier(NonNull.class)
@FunctionalInterface
public interface InvokeStaticRewrite extends MethodVisitorFactory {
    @Nullable Rewrite rewrite(ClassProcessingContext context, int opcode, String owner, String name, String descriptor, boolean isInterface);

    @Override
    default MethodVisitor createVisitor(final int api, final MethodVisitor parent, final ClassProcessingContext context) {
        return new MethodVisitor(api, parent) {
            @Override
            public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
                final @Nullable Rewrite replacement = InvokeStaticRewrite.this.rewrite(context, opcode, owner, name, descriptor, isInterface);
                if (replacement != null) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, replacement.owner, replacement.name, replacement.descriptor, replacement.isInterface);
                    return;
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
                if (bootstrapMethodHandle.getOwner().equals("java/lang/invoke/LambdaMetafactory") && bootstrapMethodArguments.length > 1 && bootstrapMethodArguments[1] instanceof Handle handle) {
                    final @Nullable Rewrite replacement = InvokeStaticRewrite.this.rewrite(context, handle.getTag(), handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface());
                    if (replacement != null) {
                        bootstrapMethodArguments[1] = new Handle(Opcodes.H_INVOKESTATIC, replacement.owner, replacement.name, replacement.descriptor, replacement.isInterface);
                        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                        return;
                    }
                }
                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            }
        };
    }

    static InvokeStaticRewrite forOwner(final String ownerClass, final InvokeStaticRewrite invokeStaticRewrite) {
        return (context, opcode, owner, name, descriptor, isInterface) -> {
            if (!owner.equals(ownerClass)) {
                return null;
            }
            return invokeStaticRewrite.rewrite(context, opcode, owner, name, descriptor, isInterface);
        };
    }

    static Rewrite rewrite(final String owner, final String name, final String descriptor, final boolean isInterface) {
        return new Rewrite(owner, name, descriptor, isInterface);
    }

    static Rewrite staticRedirect(final String owner, final String name, final String descriptor) {
        return rewrite(owner, name, descriptor, false);
    }

    static String insertFirstParam(final String insert, final String desc) {
        return "(L" + insert + ";" + desc.substring(1);
    }

    record Rewrite(
        String owner,
        String name,
        String descriptor,
        boolean isInterface
    ) {
    }
}

package io.papermc.reflectionrewriter;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@FunctionalInterface
interface InvokeStaticRewrite extends VisitInvokeDynamicInsn, VisitMethodInsn {

    @Nullable Rewrite visitInvokeStatic(MethodVisitor parent, String owner, String name, String descriptor, boolean isInterface);

    @Override
    default boolean visit(final MethodVisitor parent, final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
        if (bootstrapMethodHandle.getOwner().equals("java/lang/invoke/LambdaMetafactory") && bootstrapMethodArguments.length > 1 && bootstrapMethodArguments[1] instanceof Handle handle) {
            final @Nullable Rewrite replacement = this.visitInvokeStatic(parent, handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface());
            if (replacement != null) {
                bootstrapMethodArguments[1] = new Handle(Opcodes.H_INVOKESTATIC, replacement.owner, replacement.name, replacement.descriptor, replacement.isInterface);
                parent.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean visit(final MethodVisitor parent, final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        final @Nullable Rewrite replacement = this.visitInvokeStatic(parent, owner, name, descriptor, isInterface);
        if (replacement != null) {
            parent.visitMethodInsn(Opcodes.INVOKESTATIC, replacement.owner, replacement.name, replacement.descriptor, replacement.isInterface);
            return true;
        }
        return false;
    }

    static InvokeStaticRewrite forOwner(final String ownerClass, final InvokeStaticRewrite invokeStaticRewrite) {
        return (parent, owner, name, descriptor, isInterface) -> {
            if (!owner.equals(ownerClass)) {
                return null;
            }
            return invokeStaticRewrite.visitInvokeStatic(parent, owner, name, descriptor, isInterface);
        };
    }

    static Rewrite rewrite(final String owner, final String name, final String descriptor, final boolean isInterface) {
        return new Rewrite(owner, name, descriptor, isInterface);
    }

    static Rewrite staticRedirect(final String owner, final String name, final String descriptor) {
        return rewrite(owner, name, descriptor, false);
    }

    final class Rewrite {
        String owner;
        String name;
        String descriptor;
        boolean isInterface;

        Rewrite(final String owner, final String name, final String descriptor, final boolean isInterface) {
            this.owner = owner;
            this.name = name;
            this.descriptor = descriptor;
            this.isInterface = isInterface;
        }
    }
}

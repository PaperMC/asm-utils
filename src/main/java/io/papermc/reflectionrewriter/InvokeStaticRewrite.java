package io.papermc.reflectionrewriter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@DefaultQualifier(NonNull.class)
@FunctionalInterface
interface InvokeStaticRewrite extends MethodVisitorFactory {
    @Nullable Rewrite rewrite(ClassInfoProvider classInfoProvider, String owner, String name, String descriptor, boolean isInterface);

    @Override
    default MethodVisitor createVisitor(final int api, final MethodVisitor parent, final ClassInfoProvider classInfoProvider) {
        return new MethodVisitor(api, parent) {
            @Override
            public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
                final @Nullable Rewrite replacement = InvokeStaticRewrite.this.rewrite(classInfoProvider, owner, name, descriptor, isInterface);
                if (replacement != null) {
                    parent.visitMethodInsn(Opcodes.INVOKESTATIC, replacement.owner, replacement.name, replacement.descriptor, replacement.isInterface);
                    return;
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
                if (bootstrapMethodHandle.getOwner().equals("java/lang/invoke/LambdaMetafactory") && bootstrapMethodArguments.length > 1 && bootstrapMethodArguments[1] instanceof Handle handle) {
                    final @Nullable Rewrite replacement = InvokeStaticRewrite.this.rewrite(classInfoProvider, handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface());
                    if (replacement != null) {
                        bootstrapMethodArguments[1] = new Handle(Opcodes.H_INVOKESTATIC, replacement.owner, replacement.name, replacement.descriptor, replacement.isInterface);
                        parent.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                        return;
                    }
                }
                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            }
        };
    }

    static InvokeStaticRewrite forOwner(final String ownerClass, final InvokeStaticRewrite invokeStaticRewrite) {
        return (classInfoProvider, owner, name, descriptor, isInterface) -> {
            if (!owner.equals(ownerClass)) {
                return null;
            }
            return invokeStaticRewrite.rewrite(classInfoProvider, owner, name, descriptor, isInterface);
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

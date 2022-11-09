package io.papermc.reflectionrewriter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

@DefaultQualifier(NonNull.class)
public final class MethodVisitorBuilder implements MethodVisitorFactory {
    private @Nullable VisitMethodInsn visitMethodInsn;
    private @Nullable VisitInvokeDynamicInsn visitInvokeDynamicInsn;

    public MethodVisitorBuilder visitMethodInsn(final VisitMethodInsn visitMethodInsn) {
        this.visitMethodInsn = visitMethodInsn;
        return this;
    }

    public MethodVisitorBuilder visitInvokeDynamicInsn(final VisitInvokeDynamicInsn visitInvokeDynamicInsn) {
        this.visitInvokeDynamicInsn = visitInvokeDynamicInsn;
        return this;
    }

    public <T extends VisitMethodInsn & VisitInvokeDynamicInsn> MethodVisitorBuilder visitBoth(final T visitor) {
        return this.visitMethodInsn(visitor).visitInvokeDynamicInsn(visitor);
    }

    @Override
    public MethodVisitor createVisitor(final int api, final MethodVisitor parent, final ClassInfoProvider classInfoProvider) {
        return new BuiltVisitor(api, parent, this.visitMethodInsn, this.visitInvokeDynamicInsn);
    }

    private static final class BuiltVisitor extends MethodVisitor {
        private final @Nullable VisitMethodInsn visitMethodInsn;
        private final @Nullable VisitInvokeDynamicInsn visitInvokeDynamicInsn;

        private BuiltVisitor(
            final int api,
            final MethodVisitor parent,
            final @Nullable VisitMethodInsn visitMethodInsn,
            final @Nullable VisitInvokeDynamicInsn visitInvokeDynamicInsn
        ) {
            super(api, parent);
            this.visitMethodInsn = visitMethodInsn;
            this.visitInvokeDynamicInsn = visitInvokeDynamicInsn;
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
            if (this.visitMethodInsn == null || !this.visitMethodInsn.visit(
                this.mv,
                opcode, owner, name, descriptor, isInterface
            )) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }
        }

        @Override
        public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
            if (this.visitInvokeDynamicInsn == null || !this.visitInvokeDynamicInsn.visit(
                this.mv,
                name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments
            )) {
                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            }
        }
    }
}

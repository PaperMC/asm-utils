package io.papermc.reflectionrewriter;

import org.objectweb.asm.MethodVisitor;

@FunctionalInterface
public interface VisitMethodInsn {
    boolean visit(MethodVisitor parent, int opcode, String owner, String name, String descriptor, boolean isInterface);

    static VisitMethodInsn forOwner(final String ownerClass, final VisitMethodInsn visitMethodInsn) {
        return (parent, opcode, owner, name, descriptor, isInterface) -> {
            if (!owner.equals(ownerClass)) {
                // Continue up the chain
                return false;
            }
            return visitMethodInsn.visit(parent, opcode, owner, name, descriptor, isInterface);
        };
    }
}

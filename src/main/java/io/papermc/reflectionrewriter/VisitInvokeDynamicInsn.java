package io.papermc.reflectionrewriter;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

@FunctionalInterface
public interface VisitInvokeDynamicInsn {
    boolean visit(MethodVisitor parent, String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments);
}

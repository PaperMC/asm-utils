package io.papermc.reflectionrewriter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.MethodVisitor;

@DefaultQualifier(NonNull.class)
@FunctionalInterface
public interface MethodVisitorFactory {
    MethodVisitor createVisitor(int api, MethodVisitor parent, ClassInfoProvider classInfoProvider);
}

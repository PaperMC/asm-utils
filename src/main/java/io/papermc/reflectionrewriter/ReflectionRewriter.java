package io.papermc.reflectionrewriter;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

@DefaultQualifier(NonNull.class)
public final class ReflectionRewriter extends ClassVisitor implements ClassProcessingContext {
    private final RewriteRules rules;
    private final ClassInfoProvider classInfoProvider;
    private @MonotonicNonNull String name;
    private @Nullable String superName;

    public ReflectionRewriter(
        final int api,
        final ClassVisitor parent,
        final RewriteRules rules,
        final ClassInfoProvider classInfoProvider
    ) {
        super(api, parent);
        this.rules = rules;
        this.classInfoProvider = classInfoProvider;
    }

    @Override
    public void visit(final int version, final int access, final String name, final @Nullable String signature, final @Nullable String superName, final String @Nullable [] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.name = name;
        this.superName = superName;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final @Nullable String signature, final String @Nullable [] exceptions) {
        return this.rules.methodVisitor(
            this.api,
            super.visitMethod(access, name, descriptor, signature, exceptions),
            this
        );
    }

    @Override
    public ClassInfoProvider classInfoProvider() {
        return this.classInfoProvider;
    }

    @Override
    public String processingClassName() {
        return this.name;
    }

    @Override
    public @Nullable String processingClassSuperClassName() {
        return this.superName;
    }
}

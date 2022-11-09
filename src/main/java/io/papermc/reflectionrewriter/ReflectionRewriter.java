package io.papermc.reflectionrewriter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public final class ReflectionRewriter extends ClassVisitor {
    private final RewriteRules rules;
    private final ClassInfoProvider classInfoProvider;

    public ReflectionRewriter(final int api, final ClassVisitor parent, final RewriteRules rules, final ClassInfoProvider classInfoProvider) {
        super(api, parent);
        this.rules = rules;
        this.classInfoProvider = classInfoProvider;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
        return this.rules.methodVisitor(this.api, super.visitMethod(access, name, descriptor, signature, exceptions), this.classInfoProvider);
    }
}

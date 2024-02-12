package io.papermc.asm;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.MethodVisitor;

@DefaultQualifier(NonNull.class)
public record BasicRewriteRule(
    ShouldProcess shouldProcess,
    MethodVisitorFactory methodVisitorFactory
) implements RewriteRule {
    @Override
    public boolean shouldProcess(final ClassProcessingContext context) {
        return this.shouldProcess.shouldProcess(context);
    }

    @Override
    public MethodVisitor createVisitor(
        final int api,
        final MethodVisitor parent,
        final ClassProcessingContext context
    ) {
        return this.methodVisitorFactory.createVisitor(api, parent, context);
    }
}

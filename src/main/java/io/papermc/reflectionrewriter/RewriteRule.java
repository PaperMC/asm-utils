package io.papermc.reflectionrewriter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public record RewriteRule(MethodVisitorFactory methodVisitorFactory) {
    public static RewriteRule create(final MethodVisitorFactory methodVisitorFactory) {
        return new RewriteRule(methodVisitorFactory);
    }
}

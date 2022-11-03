package io.papermc.reflectionrewriter;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public record RewriteRule(MethodVisitorFactory methodVisitorFactory) {
    public static RewriteRule methodVisitorBuilder(final Consumer<MethodVisitorBuilder> op) {
        final MethodVisitorBuilder builder = new MethodVisitorBuilder();
        op.accept(builder);
        return new RewriteRule(builder);
    }
}

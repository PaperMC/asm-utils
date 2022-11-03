package io.papermc.reflectionrewriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.MethodVisitor;

@DefaultQualifier(NonNull.class)
public record RewriteRules(List<RewriteRule> rules) {
    public RewriteRules(final List<RewriteRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public MethodVisitor methodVisitor(final int api, final MethodVisitor visitor) {
        MethodVisitor lastVisitor = visitor;
        for (final RewriteRule rule : this.rules()) {
            lastVisitor = rule.methodVisitorFactory().createVisitor(api, lastVisitor);
        }
        return lastVisitor;
    }

    public static final class Builder {
        private final List<RewriteRule> rules = new ArrayList<>();

        public Builder rule(final RewriteRule rule) {
            this.rules.add(rule);
            return this;
        }

        public Builder rule(final RewriteRule... rules) {
            return this.rules(Arrays.asList(rules));
        }

        public Builder rules(final List<RewriteRule> rules) {
            this.rules.addAll(rules);
            return this;
        }

        public Builder builtInRules(final String proxyClassName, final ClassInfoProvider classInfoProvider) {
            return this.rules(new BuiltInRules(proxyClassName, classInfoProvider).builtInRules());
        }

        public RewriteRules build() {
            return new RewriteRules(this.rules);
        }
    }
}

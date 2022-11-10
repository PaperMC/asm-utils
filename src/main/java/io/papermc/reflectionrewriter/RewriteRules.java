package io.papermc.reflectionrewriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.MethodVisitor;

@DefaultQualifier(NonNull.class)
public final class RewriteRules {
    private final RewriteRule chained;

    public RewriteRules(final List<RewriteRule> rules) {
        this.chained = RewriteRule.chain(rules);
    }

    public MethodVisitor methodVisitor(
        final int api,
        final MethodVisitor visitor,
        final ClassProcessingContext context
    ) {
        if (this.chained.shouldProcess(context)) {
            return this.chained.createVisitor(api, visitor, context);
        }
        return visitor;
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

        public Builder defaultRules(final String proxyClassName) {
            return this.rules(new DefaultRules(proxyClassName).rules());
        }

        public RewriteRules build() {
            return new RewriteRules(this.rules);
        }
    }
}

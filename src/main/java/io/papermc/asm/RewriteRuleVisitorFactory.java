package io.papermc.asm;

import io.papermc.asm.rules.RewriteRule;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.objectweb.asm.ClassVisitor;

public interface RewriteRuleVisitorFactory {
    ClassVisitor createVisitor(ClassVisitor parent);

    static RewriteRuleVisitorFactory create(
        final int api,
        final Supplier<RewriteRule> ruleFactory,
        final ClassInfoProvider classInfoProvider
    ) {
        return new AbstractRewriteRuleVisitorFactory(api, classInfoProvider) {
            @Override
            protected RewriteRule createRule() {
                return ruleFactory.get();
            }
        };
    }

    static RewriteRuleVisitorFactory create(
        final int api,
        final RewriteRule rule,
        final ClassInfoProvider classInfoProvider
    ) {
        return create(api, () -> rule, classInfoProvider);
    }

    static RewriteRuleVisitorFactory create(
        final int api,
        final Consumer<RewriteRule.ChainBuilder> builderConsumer,
        final ClassInfoProvider classInfoProvider
    ) {
        return create(api, () -> {
            final RewriteRule.ChainBuilder builder = RewriteRule.chain();
            builderConsumer.accept(builder);
            return builder.build();
        }, classInfoProvider);
    }
}

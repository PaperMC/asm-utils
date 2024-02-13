package io.papermc.asm;

import io.papermc.asm.rules.RewriteRule;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.ClassVisitor;

@DefaultQualifier(NonNull.class)
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
}

package io.papermc.asm.versioned;

import io.papermc.asm.rules.RewriteRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface VersionedRuleFactory {

    static VersionedRuleFactory chain(final VersionedRuleFactory... factories) {
        return chain(Arrays.asList(factories));
    }

    static VersionedRuleFactory chain(final Collection<? extends VersionedRuleFactory> factories) {
        return new Chain(List.copyOf(factories));
    }

    @Nullable RewriteRule createRule(ApiVersion apiVersion);

    record Chain(List<VersionedRuleFactory> factories) implements VersionedRuleFactory{

        public Chain {
            factories = List.copyOf(factories);
        }

        @Override
        public RewriteRule createRule(final ApiVersion apiVersion) {
            final List<RewriteRule> rules = new ArrayList<>();
            for (final VersionedRuleFactory factory : this.factories) {
                final @Nullable RewriteRule rule = factory.createRule(apiVersion);
                if (rule != null) {
                    rules.add(rule);
                }
            }
            return RewriteRule.chain(rules);
        }
    }
}

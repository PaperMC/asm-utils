package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import java.util.Set;
import java.util.function.Consumer;

public class ConfiguredRuleFactoryImpl extends RuleFactoryImpl implements ConfiguredRuleFactory {

    private final RuleFactoryConfiguration config;

    ConfiguredRuleFactoryImpl(final Set<Class<?>> owners, final RuleFactoryConfiguration config) {
        super(owners);
        this.config = config;
    }

    @Override
    public void plainStaticRewrite(final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        this.plainStaticRewrite(this.config.delegateOwner(), builderConsumer);
    }
}

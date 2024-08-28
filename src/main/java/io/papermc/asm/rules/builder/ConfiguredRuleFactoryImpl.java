package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import java.lang.constant.ClassDesc;
import java.util.Set;

public class ConfiguredRuleFactoryImpl extends RuleFactoryImpl implements ConfiguredRuleFactory {

    private final RuleFactoryConfiguration config;

    ConfiguredRuleFactoryImpl(final Set<ClassDesc> owners, final RuleFactoryConfiguration config) {
        super(owners);
        this.config = config;
    }

    @Override
    public void plainStaticRewrite(final MethodMatcher methodMatcher) {
        this.plainStaticRewrite(this.config.delegateOwner(), methodMatcher);
    }
}

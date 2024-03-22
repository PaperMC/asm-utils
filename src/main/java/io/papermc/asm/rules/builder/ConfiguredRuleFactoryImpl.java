package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
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

    @Override
    public void changeParamFuzzy(final ClassDesc newParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeParamFuzzy(this.config.generatedDelegateOwner(), newParamType, staticHandler, builderConsumer);
    }

    @Override
    public void changeParamDirect(final ClassDesc newParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeParamDirect(this.config.generatedDelegateOwner(), newParamType, staticHandler, builderConsumer);
    }

    @Override
    public void changeReturnTypeFuzzy(final ClassDesc newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeFuzzy(this.config.generatedDelegateOwner(), newReturnType, staticHandler, builderConsumer);
    }

    @Override
    public void changeReturnTypeDirect(final ClassDesc newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirect(this.config.generatedDelegateOwner(), newReturnType, staticHandler, builderConsumer);
    }

    @Override
    public void changeReturnTypeFuzzyWithContext(final ClassDesc newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeFuzzyWithContext(this.config.generatedDelegateOwner(), newReturnType, staticHandler, builderConsumer);
    }

    @Override
    public void changeReturnTypeDirectWithContext(final ClassDesc newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirectWithContext(this.config.generatedDelegateOwner(), newReturnType, staticHandler, builderConsumer);
    }
}

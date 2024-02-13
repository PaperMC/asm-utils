package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfiguredRuleFactoryImpl extends RuleFactoryImpl implements ConfiguredRuleFactory {

    private final Class<?> delegateOwner;
    private final Supplier<Class<?>> generatedDelegateOwner;

    ConfiguredRuleFactoryImpl(final Set<Class<?>> owners, final Class<?> delegateOwner, final Supplier<Class<?>> generatedDelegateOwner) {
        super(owners);
        this.delegateOwner = delegateOwner;
        this.generatedDelegateOwner = generatedDelegateOwner;
    }

    @Override
    public void plainStaticRewrite(final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        this.plainStaticRewrite(this.delegateOwner, builderConsumer);
    }

    @Override
    public void changeParamFuzzy(final Class<?> newParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeParamFuzzy(this.generatedDelegateOwner, newParamType, staticHandler, builderConsumer);
    }

    @Override
    public void changeParamDirect(final Class<?> newParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeParamDirect(this.generatedDelegateOwner, newParamType, staticHandler, builderConsumer);
    }

    @Override
    public void changeReturnTypeFuzzy(final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeFuzzy(this.generatedDelegateOwner, newReturnType, staticHandler, builderConsumer);
    }

    @Override
    public void changeReturnTypeDirect(final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirect(this.generatedDelegateOwner, newReturnType, staticHandler, builderConsumer);
    }

    @Override
    public void changeReturnTypeFuzzyWithContext(final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeFuzzyWithContext(this.generatedDelegateOwner, newReturnType, staticHandler, builderConsumer);
    }

    @Override
    public void changeReturnTypeDirectWithContext(final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirectWithContext(this.generatedDelegateOwner, newReturnType, staticHandler, builderConsumer);
    }
}

package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ConfiguredRuleFactory extends RuleFactory {

    static ConfiguredRuleFactory create(final Set<Class<?>> owners, final Class<?> delegateOwner, final Supplier<Class<?>> generatedDelegateOwner) {
        return new ConfiguredRuleFactoryImpl(owners, delegateOwner, generatedDelegateOwner);
    }

    @SafeVarargs
    static Consumer<? super ConfiguredRuleFactory> combine(final Consumer<? super ConfiguredRuleFactory>...factories) {
        return r -> {
            for (final Consumer<? super ConfiguredRuleFactory> factory : factories) {
                factory.accept(r);
            }
        };
    }

    void plainStaticRewrite(Consumer<? super MethodMatcher.Builder> builderConsumer);

    void changeParamFuzzy(Class<?> newParamType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeParamDirect(Class<?> newParamType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeReturnTypeFuzzy(Class<?> newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeReturnTypeDirect(Class<?> newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeReturnTypeFuzzyWithContext(Class<?> newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeReturnTypeDirectWithContext(Class<?> newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);
}

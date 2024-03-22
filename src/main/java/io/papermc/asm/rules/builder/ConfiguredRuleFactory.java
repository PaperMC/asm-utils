package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Consumer;

import static io.papermc.asm.util.DescriptorUtils.desc;

public interface ConfiguredRuleFactory extends RuleFactory {

    static ConfiguredRuleFactory create(final Set<Class<?>> owners, final RuleFactoryConfiguration config) {
        return new ConfiguredRuleFactoryImpl(owners, config);
    }

    static ConfiguredRuleFactory.Factory combine(final ConfiguredRuleFactory.Factory... factories) {
        return r -> {
            for (final ConfiguredRuleFactory.Factory factory : factories) {
                factory.accept(r);
            }
        };
    }

    void plainStaticRewrite(Consumer<? super MethodMatcher.Builder> builderConsumer);

    default void changeParamFuzzy(final Class<?> newParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeParamFuzzy(desc(newParamType), staticHandler, builderConsumer);
    }

    void changeParamFuzzy(ClassDesc newParamType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeParamDirect(final Class<?> newParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeParamDirect(desc(newParamType), staticHandler, builderConsumer);
    }

    void changeParamDirect(ClassDesc newParamType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeFuzzy(final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeFuzzy(desc(newReturnType), staticHandler, builderConsumer);
    }

    void changeReturnTypeFuzzy(ClassDesc newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeDirect(final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirect(desc(newReturnType), staticHandler, builderConsumer);
    }

    void changeReturnTypeDirect(ClassDesc newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeFuzzyWithContext(final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeFuzzyWithContext(desc(newReturnType), staticHandler, builderConsumer);
    }

    void changeReturnTypeFuzzyWithContext(ClassDesc newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeDirectWithContext(final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirectWithContext(desc(newReturnType), staticHandler, builderConsumer);
    }

    void changeReturnTypeDirectWithContext(ClassDesc newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    @FunctionalInterface
    interface Factory extends Consumer<ConfiguredRuleFactory> {

        @Override
        void accept(ConfiguredRuleFactory factory);
    }
}

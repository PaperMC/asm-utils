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

    @FunctionalInterface
    interface Factory extends Consumer<ConfiguredRuleFactory> {

        @Override
        void accept(ConfiguredRuleFactory factory);
    }
}

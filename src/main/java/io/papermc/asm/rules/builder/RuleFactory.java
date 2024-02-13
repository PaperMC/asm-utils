package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.FieldMatcher;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.papermc.asm.util.DescriptorUtils.desc;

public interface RuleFactory {

    static RuleFactory create(final Set<Class<?>> owners) {
        return new RuleFactoryImpl(owners);
    }

    @SafeVarargs
    static Consumer<? super RuleFactory> combine(final Consumer<? super RuleFactory>...factories) {
        return r -> {
            for (final Consumer<? super RuleFactory> factory : factories) {
                factory.accept(r);
            }
        };
    }

    default void plainStaticRewrite(final Class<?> newOwner, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        this.plainStaticRewrite(desc(newOwner), builderConsumer);
    }

    default void plainStaticRewrite(final String newOwner, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        this.plainStaticRewrite(ClassDesc.of(newOwner), builderConsumer);
    }

    void plainStaticRewrite(ClassDesc newOwner, Consumer<? super MethodMatcher.Builder> builderConsumer);

    void changeParamToSuper(Class<?> oldParamType, Class<?> newParamType, Consumer<? super MethodMatcher.Builder> builderConsumer);

    void changeParamFuzzy(Supplier<Class<?>> newOwner, Class<?> newParamType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeParamDirect(Supplier<Class<?>> newOwner, Class<?> newParamType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeReturnTypeToSub(Class<?> oldReturnType, Class<?> newReturnType, Consumer<? super MethodMatcher.Builder> builderConsumer);

    void changeReturnTypeFuzzy(Supplier<Class<?>> newOwner, Class<?> newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeReturnTypeDirect(Supplier<Class<?>> newOwner, Class<?> newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeReturnTypeFuzzyWithContext(Supplier<Class<?>> newOwner, Class<?> newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeReturnTypeDirectWithContext(Supplier<Class<?>> newOwner, Class<?> newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void renameField(String newName, Consumer<? super FieldMatcher.Builder> builderConsumer);

    void addRule(RewriteRule rule);

    RewriteRule build();
}

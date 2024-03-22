package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.FieldMatcher;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Consumer;

import static io.papermc.asm.util.DescriptorUtils.desc;

public interface RuleFactory {

    static RuleFactory create(final Set<Class<?>> owners) {
        return new RuleFactoryImpl(owners);
    }

    static RuleFactory.Factory combine(final RuleFactory.Factory... factories) {
        return r -> {
            for (final RuleFactory.Factory factory : factories) {
                factory.accept(r);
            }
        };
    }

    void plainStaticRewrite(ClassDesc newOwner, Consumer<? super MethodMatcher.Builder> builderConsumer);

    default void changeParamToSuper(final Class<?> oldParamType, final Class<?> newParamType, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        if (!newParamType.isAssignableFrom(oldParamType)) {
            throw new IllegalArgumentException(newParamType + " is not a superclass of " + oldParamType);
        }
        this.changeParamToSuper(desc(oldParamType), desc(newParamType), builderConsumer);
    }

    void changeParamToSuper(ClassDesc oldParamType, ClassDesc newParamType, Consumer<? super MethodMatcher.Builder> builderConsumer);

    default void changeParamFuzzy(final ClassDesc newOwner, final Class<?> newParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeParamFuzzy(newOwner, desc(newParamType), staticHandler, builderConsumer);
    }

    void changeParamFuzzy(ClassDesc newOwner, ClassDesc newParamType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeParamDirect(final ClassDesc newOwner, final Class<?> newParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeParamDirect(newOwner, desc(newParamType), staticHandler, builderConsumer);
    }

    void changeParamDirect(ClassDesc newOwner, ClassDesc newParamType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeToSub(final Class<?> oldReturnType, final Class<?> newReturnType, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        if (!oldReturnType.isAssignableFrom(newReturnType)) {
            throw new IllegalArgumentException(newReturnType + " is not a subclass of " + oldReturnType);
        }
        this.changeReturnTypeToSub(desc(oldReturnType), desc(newReturnType), builderConsumer);
    }

    void changeReturnTypeToSub(ClassDesc oldReturnType, ClassDesc newReturnType, Consumer<? super MethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeFuzzy(final ClassDesc newOwner, final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeFuzzy(newOwner, desc(newReturnType), staticHandler, builderConsumer);
    }

    void changeReturnTypeFuzzy(ClassDesc newOwner, ClassDesc newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeDirect(final ClassDesc newOwner, final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirect(newOwner, desc(newReturnType), staticHandler, builderConsumer);
    }

    void changeReturnTypeDirect(ClassDesc newOwner, ClassDesc newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeFuzzyWithContext(final ClassDesc newOwner, final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeFuzzyWithContext(newOwner, desc(newReturnType), staticHandler, builderConsumer);
    }

    void changeReturnTypeFuzzyWithContext(ClassDesc newOwner, ClassDesc newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeDirectWithContext(final ClassDesc newOwner, final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirectWithContext(newOwner, desc(newReturnType), staticHandler, builderConsumer);
    }

    void changeReturnTypeDirectWithContext(ClassDesc newOwner, ClassDesc newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void renameField(String newName, Consumer<? super FieldMatcher.Builder> builderConsumer);

    void addRule(RewriteRule rule);

    RewriteRule build();

    @FunctionalInterface
    interface Factory extends Consumer<RuleFactory> {

        @Override
        void accept(RuleFactory factory);
    }
}

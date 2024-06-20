package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.FieldMatcher;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

import static io.papermc.asm.util.DescriptorUtils.desc;

public interface RuleFactory {

    static RuleFactory create(final Set<ClassDesc> owners) {
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

    void changeParamToSuper(ClassDesc legacyParamType, ClassDesc newParamType, Consumer<? super MethodMatcher.Builder> builderConsumer);

    default void changeParamFuzzy(final Class<?> newParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeParamFuzzy(desc(newParamType), staticHandler, builderConsumer);
    }

    void changeParamFuzzy(ClassDesc newParamType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeParamDirect(final Class<?> newParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeParamDirect(desc(newParamType), staticHandler, builderConsumer);
    }

    void changeParamDirect(ClassDesc newParamType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeToSub(final Class<?> oldReturnType, final Class<?> newReturnType, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        if (!oldReturnType.isAssignableFrom(newReturnType)) {
            throw new IllegalArgumentException(newReturnType + " is not a subclass of " + oldReturnType);
        }
        this.changeReturnTypeToSub(desc(oldReturnType), desc(newReturnType), builderConsumer);
    }

    void changeReturnTypeToSub(ClassDesc oldReturnType, ClassDesc newReturnType, Consumer<? super MethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeDirect(final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirect(desc(newReturnType), staticHandler, builderConsumer);
    }

    void changeReturnTypeDirect(ClassDesc newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    default void changeReturnTypeDirectWithContext(final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirectWithContext(desc(newReturnType), staticHandler, builderConsumer);
    }

    void changeReturnTypeDirectWithContext(ClassDesc newReturnType, Method staticHandler, Consumer<? super TargetedMethodMatcher.Builder> builderConsumer);

    void changeFieldToMethod(Consumer<? super FieldMatcher.Builder> builderConsumer, @Nullable String getterName, @Nullable String setterName, boolean isInterfaceMethod);

    void addRule(RewriteRule rule);

    RewriteRule build();

    @FunctionalInterface
    interface Factory extends Consumer<RuleFactory> {

        @Override
        void accept(RuleFactory factory);
    }
}

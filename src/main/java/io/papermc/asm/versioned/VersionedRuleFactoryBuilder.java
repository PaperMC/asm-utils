package io.papermc.asm.versioned;

import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import io.papermc.asm.versioned.matcher.VersionedMethodMatcher;
import io.papermc.asm.versioned.matcher.targeted.VersionedTargetedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
import java.util.Set;

import static io.papermc.asm.util.DescriptorUtils.desc;

public interface VersionedRuleFactoryBuilder {

    static VersionedRuleFactoryBuilder create(final Set<ClassDesc> owners) {
        return new VersionedRuleFactoryBuilderImpl(owners);
    }

    // plain static rewrite

    //<editor-fold desc="changeParamToSuper" defaultstate="collapsed">
    default void changeParamToSuper(final Class<?> newParamType, final ApiVersion apiVersion, final Class<?> legacyParamType, final MethodMatcher methodMatcher) {
        this.changeParamToSuper(desc(newParamType), apiVersion, desc(legacyParamType), methodMatcher);
    }

    default void changeParamToSuper(final ClassDesc newParamType, final ApiVersion apiVersion, final ClassDesc legacyParamType, final MethodMatcher methodMatcher) {
        this.changeParamToSuper(newParamType, VersionedMethodMatcher.single(apiVersion, methodMatcher, legacyParamType));
    }

    default void changeParamToSuper(final Class<?> newParamType, final VersionedMethodMatcher versions) {
        this.changeParamToSuper(desc(newParamType), versions);
    }

    void changeParamToSuper(ClassDesc newParamType, VersionedMethodMatcher versions);
    //</editor-fold>

    //<editor-fold desc="changeParamFuzzy" defaultstate="collapsed">
    default void changeParamFuzzy(final Class<?> newParamType, final ApiVersion apiVersion, final Method staticHandler, final TargetedMethodMatcher targetedMethodMatcher) {
        this.changeParamFuzzy(desc(newParamType), apiVersion, staticHandler, targetedMethodMatcher);
    }

    default void changeParamFuzzy(final ClassDesc newParamType, final ApiVersion apiVersion, final Method staticHandler, final TargetedMethodMatcher targetedMethodMatcher) {
        this.changeParamFuzzy(newParamType, VersionedTargetedMethodMatcher.single(apiVersion, targetedMethodMatcher, staticHandler));
    }

    default void changeParamFuzzy(final Class<?> newParamType, final VersionedTargetedMethodMatcher versions) {
        this.changeParamFuzzy(desc(newParamType), versions);
    }

    void changeParamFuzzy(ClassDesc newParamType, VersionedTargetedMethodMatcher versions);
    //</editor-fold>

    //<editor-fold desc="changeParamDirect" defaultstate="collapsed">
    default void changeParamDirect(final Class<?> newParamType, final ApiVersion apiVersion, final Method staticHandler, final TargetedMethodMatcher methodMatcher) {
        this.changeParamDirect(desc(newParamType), apiVersion, staticHandler, methodMatcher);
    }

    default void changeParamDirect(final ClassDesc newParamType, final ApiVersion apiVersion, final Method staticHandler, final TargetedMethodMatcher methodMatcher) {
        this.changeParamDirect(newParamType, VersionedTargetedMethodMatcher.single(apiVersion, methodMatcher, staticHandler));
    }

    default void changeParamDirect(final Class<?> newParamType, final VersionedTargetedMethodMatcher versions) {
        this.changeParamDirect(desc(newParamType), versions);
    }

    void changeParamDirect(ClassDesc newParamType, VersionedTargetedMethodMatcher versions);
    //</editor-fold>

    //<editor-fold desc="changeReturnTypeToSub" defaultstate="collapsed">
    default void changeReturnTypeToSub(final Class<?> newReturnType, final ApiVersion apiVersion, final Class<?> legacyReturnType, final MethodMatcher methodMatcher) {
        this.changeReturnTypeToSub(desc(newReturnType), apiVersion, desc(legacyReturnType), methodMatcher);
    }

    default void changeReturnTypeToSub(final ClassDesc newReturnType, final ApiVersion apiVersion, final ClassDesc legacyReturnType, final MethodMatcher methodMatcher) {
        this.changeReturnTypeToSub(newReturnType, VersionedMethodMatcher.single(apiVersion, methodMatcher, legacyReturnType));
    }

    default void changeReturnTypeToSub(final Class<?> newReturnType, final VersionedMethodMatcher versions) {
        this.changeReturnTypeToSub(desc(newReturnType), versions);
    }

    void changeReturnTypeToSub(ClassDesc newReturnType, VersionedMethodMatcher versions);
    //</editor-fold>

    VersionedRuleFactory build();
}

package io.papermc.asm.versioned;

import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static io.papermc.asm.util.DescriptorUtils.desc;

public interface VersionedRuleFactoryBuilder {

    static VersionedRuleFactoryBuilder create(final Set<ClassDesc> owners) {
        return new VersionedRuleFactoryBuilderImpl(owners);
    }

    default void changeParamDirect(final Class<?> newParamType, final ApiVersion apiVersion, final TargetedMethodMatcher methodMatcher, final Method staticHandler) {
        this.changeParamDirect(desc(newParamType), apiVersion, methodMatcher, staticHandler);
    }

    default void changeParamDirect(final ClassDesc newParamType, final ApiVersion apiVersion, final TargetedMethodMatcher methodMatcher, final Method staticHandler) {
        this.changeParamDirect(newParamType, new TreeMap<>(Map.of(apiVersion, Map.entry(methodMatcher, staticHandler))));
    }

    default void changeParamDirect(final Class<?> newParamType, final NavigableMap<ApiVersion, Map.Entry<TargetedMethodMatcher, Method>> versions) {
        this.changeParamDirect(desc(newParamType), versions);
    }

    void changeParamDirect(ClassDesc newParamType, NavigableMap<ApiVersion, Map.Entry<TargetedMethodMatcher, Method>> versions);

    VersionedRuleFactory build();
}

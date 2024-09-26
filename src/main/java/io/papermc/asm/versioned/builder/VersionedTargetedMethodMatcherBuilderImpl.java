package io.papermc.asm.versioned.builder;

import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import io.papermc.asm.versioned.ApiVersion;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class VersionedTargetedMethodMatcherBuilderImpl implements VersionedTargetedMethodMatcherBuilder {

    private final NavigableMap<ApiVersion, Map.Entry<TargetedMethodMatcher, Method>> versions = new TreeMap<>();

    @Override
    public VersionedTargetedMethodMatcherBuilder with(final ApiVersion apiVersion, final TargetedMethodMatcher targetedMethodMatcher, final Method method) {
        if (this.versions.containsKey(apiVersion)) {
            throw new IllegalArgumentException("Duplicate version: " + apiVersion);
        }
        this.versions.put(apiVersion, Map.entry(targetedMethodMatcher, method));
        return this;
    }

    @Override
    public NavigableMap<ApiVersion, Map.Entry<TargetedMethodMatcher, Method>> build() {
        return Collections.unmodifiableNavigableMap(this.versions);
    }
}

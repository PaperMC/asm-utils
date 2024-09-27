package io.papermc.asm.versioned.matcher.targeted;

import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import io.papermc.asm.versioned.ApiVersion;
import java.lang.reflect.Method;
import java.util.NavigableMap;
import java.util.TreeMap;

public class VersionedTargetedMethodMatcherBuilderImpl implements VersionedTargetedMethodMatcherBuilder {

    private final NavigableMap<ApiVersion, VersionedTargetedMethodMatcher.Pair> versions = new TreeMap<>();

    @Override
    public VersionedTargetedMethodMatcherBuilder with(final ApiVersion apiVersion, final TargetedMethodMatcher matcher, final Method staticHandler) {
        if (this.versions.containsKey(apiVersion)) {
            throw new IllegalArgumentException("Duplicate version: " + apiVersion);
        }
        this.versions.put(apiVersion, new VersionedTargetedMethodMatcher.Pair(matcher, staticHandler));
        return this;
    }

    @Override
    public VersionedTargetedMethodMatcher build() {
        return new VersionedTargetedMethodMatcher(this.versions);
    }
}

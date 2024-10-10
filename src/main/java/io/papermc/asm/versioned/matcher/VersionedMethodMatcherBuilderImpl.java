package io.papermc.asm.versioned.matcher;

import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.versioned.ApiVersion;
import java.lang.constant.ClassDesc;
import java.util.NavigableMap;
import java.util.TreeMap;

public class VersionedMethodMatcherBuilderImpl implements VersionedMethodMatcherBuilder {

    private final NavigableMap<ApiVersion, VersionedMethodMatcher.Pair> versions = new TreeMap<>();

    @Override
    public VersionedMethodMatcherBuilder with(final ApiVersion apiVersion, final MethodMatcher matcher, final ClassDesc legacyType) {
        if (this.versions.containsKey(apiVersion)) {
            throw new IllegalArgumentException("Duplicate version: " + apiVersion);
        }
        this.versions.put(apiVersion, new VersionedMethodMatcher.Pair(matcher, legacyType));
        return this;
    }

    @Override
    public VersionedMethodMatcher build() {
        return new VersionedMethodMatcher(this.versions);
    }
}

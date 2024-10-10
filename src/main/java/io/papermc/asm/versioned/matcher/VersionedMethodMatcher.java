package io.papermc.asm.versioned.matcher;

import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.versioned.ApiVersion;
import java.lang.constant.ClassDesc;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class VersionedMethodMatcher extends VersionedMatcherBase<VersionedMethodMatcher.Pair> {

    public static VersionedMethodMatcherBuilder builder() {
        return new VersionedMethodMatcherBuilderImpl();
    }

    public static VersionedMethodMatcher single(final ApiVersion apiVersion, final MethodMatcher matcher, final ClassDesc legacyType) {
        return new VersionedMethodMatcher(new TreeMap<>(Map.of(apiVersion, new Pair(matcher, legacyType))));
    }

    VersionedMethodMatcher(final NavigableMap<ApiVersion, Pair> map) {
        super(map);
    }

    public record Pair(MethodMatcher matcher, ClassDesc legacyType) {
    }
}

package io.papermc.asm.versioned.matcher.targeted;

import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import io.papermc.asm.versioned.ApiVersion;
import io.papermc.asm.versioned.matcher.VersionedMatcherBase;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class VersionedTargetedMethodMatcher extends VersionedMatcherBase<VersionedTargetedMethodMatcher.Pair> {

    public static VersionedTargetedMethodMatcherBuilder builder() {
        return new VersionedTargetedMethodMatcherBuilderImpl();
    }

    public static VersionedTargetedMethodMatcher single(final ApiVersion apiVersion, final TargetedMethodMatcher matcher, final Method staticHandler) {
        return new VersionedTargetedMethodMatcher(new TreeMap<>(Map.of(apiVersion, new Pair(matcher, staticHandler))));
    }

    VersionedTargetedMethodMatcher(final NavigableMap<ApiVersion, Pair> map) {
        super(map);
    }

    public record Pair(TargetedMethodMatcher matcher, Method staticHandler) {
    }
}

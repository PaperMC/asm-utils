package io.papermc.asm.versioned.builder;

import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import io.papermc.asm.util.Builder;
import io.papermc.asm.versioned.ApiVersion;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NavigableMap;
import org.jetbrains.annotations.Contract;

public interface VersionedTargetedMethodMatcherBuilder extends Builder<NavigableMap<ApiVersion, Map.Entry<TargetedMethodMatcher, Method>>> {

    static VersionedTargetedMethodMatcherBuilder builder() {
        return new VersionedTargetedMethodMatcherBuilderImpl();
    }

    @Contract(value = "_, _, _ -> this", mutates = "this")
    VersionedTargetedMethodMatcherBuilder with(ApiVersion apiVersion, TargetedMethodMatcher targetedMethodMatcher, Method method);
}

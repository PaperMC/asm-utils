package io.papermc.asm.versioned.matcher.targeted;

import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import io.papermc.asm.util.Builder;
import io.papermc.asm.versioned.ApiVersion;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Contract;

public interface VersionedTargetedMethodMatcherBuilder extends Builder<VersionedTargetedMethodMatcher> {

    @Contract(value = "_, _, _ -> this", mutates = "this")
    VersionedTargetedMethodMatcherBuilder with(ApiVersion apiVersion, TargetedMethodMatcher matcher, Method staticHandler);
}

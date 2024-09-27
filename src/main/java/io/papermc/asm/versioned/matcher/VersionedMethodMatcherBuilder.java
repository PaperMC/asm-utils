package io.papermc.asm.versioned.matcher;

import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.util.Builder;
import io.papermc.asm.versioned.ApiVersion;
import java.lang.constant.ClassDesc;
import org.jetbrains.annotations.Contract;

public interface VersionedMethodMatcherBuilder extends Builder<VersionedMethodMatcher> {

    @Contract(value = "_, _, _ -> this", mutates = "this")
    VersionedMethodMatcherBuilder with(ApiVersion apiVersion, MethodMatcher matcher, ClassDesc oldType);
}

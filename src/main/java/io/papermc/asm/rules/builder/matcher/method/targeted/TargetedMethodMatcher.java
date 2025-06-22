package io.papermc.asm.rules.builder.matcher.method.targeted;

import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import java.lang.constant.ClassDesc;

public interface TargetedMethodMatcher {

    static TargetedMethodMatcherBuilder builder() {
        return new TargetedMethodMatcherBuilderImpl();
    }

    ClassDesc targetType();

    MethodMatcher wrapped();
}

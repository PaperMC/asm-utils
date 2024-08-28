package io.papermc.asm.rules.method.params;

import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import io.papermc.asm.rules.method.generated.TargetedTypeGeneratedStaticRewrite;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Rewrites matching bytecode to a generated method which will invoke the static handler on all parameters that need to be converted. That
 * generated method then calls the original method with the new parameters.
 *
 * @param owners        the owners to target
 * @param existingType  the type to convert to
 * @param methodMatcher the method matcher to use which targets the legacy param type
 * @param staticHandler the method which will be used to convert the legacy type to the new type
 */
public record DirectParameterRewrite(Set<ClassDesc> owners, ClassDesc existingType, TargetedMethodMatcher methodMatcher, Method staticHandler) implements TargetedTypeGeneratedStaticRewrite.Parameter {
}

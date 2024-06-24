package io.papermc.asm.rules.method;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import java.lang.constant.ClassDesc;
import java.util.Set;

/**
 * Rewrites a method by just directly routing it to an identical static method in another class.
 *
 * @param owners              the owners to target
 * @param methodMatcher       the method matcher to use
 * @param staticRedirectOwner the owner to redirect to
 */
public record DirectStaticRewrite(Set<ClassDesc> owners, MethodMatcher methodMatcher, ClassDesc staticRedirectOwner) implements StaticRewrite {

    @Override
    public ClassDesc staticRedirectOwner(final ClassProcessingContext context) {
        return this.staticRedirectOwner;
    }
}

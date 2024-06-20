package io.papermc.asm.rules.method;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.OwnableRewriteRule;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;

/**
 * A rule that targets specific methods and owners.
 */
public interface FilteredMethodRewriteRule extends MethodRewriteRule, OwnableRewriteRule {

    MethodMatcher methodMatcher();

    @Override
    default boolean shouldProcess(final ClassProcessingContext context, final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        return this.matchesOwner(context, owner) && this.methodMatcher().matches(name, descriptor);
    }
}

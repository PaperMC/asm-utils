package io.papermc.asm.rules.method.returns;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.rules.method.OwnableMethodRewriteRule;
import io.papermc.asm.rules.method.rewrite.MethodRewrite;
import io.papermc.asm.rules.method.rewrite.SimpleRewrite;
import io.papermc.asm.versioned.ApiVersion;
import io.papermc.asm.versioned.VersionedRuleFactory;
import io.papermc.asm.versioned.matcher.VersionedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Changes a return type to a subtype of the old type. This isn't a compile break as subtypes inherit everything, but it is an ABI break.
 * We just change the return type in the descriptor and move on.
 *
 * @param owners        owners of the methods to change
 * @param methodMatcher method matcher to find methods with
 * @param oldReturnType the return type that will be found in bytecode that needs to be transformed
 * @param newReturnType the return type that is valid for existing method
 */
public record SubTypeReturnRewrite(Set<ClassDesc> owners, MethodMatcher methodMatcher, ClassDesc oldReturnType, ClassDesc newReturnType) implements OwnableMethodRewriteRule.Filtered {

    @Override
    public @Nullable MethodRewrite<?> rewrite(final ClassProcessingContext context, final boolean isInvokeDynamic, final int opcode, final ClassDesc owner, final String name, final MethodTypeDesc descriptor, final boolean isInterface) {
        if (!descriptor.returnType().equals(this.newReturnType())) {
            return new SimpleRewrite(opcode, owner, name, this.modifyMethodDescriptor(descriptor), isInterface, isInvokeDynamic);
        }
        return null;
    }

    private MethodTypeDesc modifyMethodDescriptor(final MethodTypeDesc methodDescriptor) {
        return methodDescriptor.changeReturnType(this.newReturnType());
    }

    public record Versioned(Set<ClassDesc> owners, ClassDesc newReturnType, VersionedMethodMatcher versions) implements VersionedRuleFactory {

        @Override
        public RewriteRule createRule(final ApiVersion apiVersion) {
            return this.versions.ruleForVersion(apiVersion, pair -> new SubTypeReturnRewrite(this.owners(), pair.matcher(), pair.legacyType(), this.newReturnType()));
        }
    }
}

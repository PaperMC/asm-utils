package io.papermc.asm.rules.method.params;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.rules.method.OwnableMethodRewriteRule;
import io.papermc.asm.rules.method.rewrite.MethodRewrite;
import io.papermc.asm.rules.method.rewrite.SimpleRewrite;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Set;

import static io.papermc.asm.util.DescriptorUtils.replaceParameters;
import static java.util.function.Predicate.isEqual;

/**
 * Changes a parameter type to a super type. This isn't a compile break, but it is an ABI break. We just change the
 * offending parameter in the descriptor and move on.
 *
 * @param owners        owners of the methods to change
 * @param methodMatcher method matcher to find methods with
 * @param oldParamType  the parameter type that will be found in bytecode that needs to be transformed
 * @param newParamType  the parameter type that is valid for existing method
 */
public record SuperTypeParamRewrite(Set<ClassDesc> owners, MethodMatcher methodMatcher, ClassDesc oldParamType, ClassDesc newParamType) implements OwnableMethodRewriteRule.Filtered {

    @Override
    public MethodRewrite<?> rewrite(final ClassProcessingContext context, final boolean isInvokeDynamic, final int opcode, final ClassDesc owner, final String name, final MethodTypeDesc descriptor, final boolean isInterface) {
        return new SimpleRewrite(opcode, owner, name, this.modifyMethodDescriptor(descriptor), isInterface, isInvokeDynamic);
    }

    private MethodTypeDesc modifyMethodDescriptor(final MethodTypeDesc methodDescriptor) {
        return replaceParameters(methodDescriptor, isEqual(this.oldParamType()), this.newParamType());
    }
}

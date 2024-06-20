package io.papermc.asm.rules.method;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

import static io.papermc.asm.util.DescriptorUtils.replaceParameters;
import static java.util.function.Predicate.isEqual;

/**
 * These rewrites do not redirect to a static method like {@link StaticRewrites}.
 * Instead, these rewrite methods in place, usually changing the descriptor.
 */
public final class MethodRewrites {

    private MethodRewrites() {
    }

    /**
     * Changes a parameter type to a super type. This isn't a compile break, but it is an ABI break. We just change the
     * offending parameter in the descriptor and move on.
     *
     * @param owners owners of the methods to change
     * @param methodMatcher method matcher to find methods with
     * @param oldParamType the parameter type that will be found in bytecode that needs to be transformed
     * @param newParamType the parameter type that is valid for existing method
     */
    public record SuperTypeParam(Set<Class<?>> owners, MethodMatcher methodMatcher, ClassDesc oldParamType, ClassDesc newParamType) implements FilteredMethodRewriteRule {

        @Override
        public Rewrite<?> rewrite(final ClassProcessingContext context, final boolean isInvokeDynamic, final int opcode, final ClassDesc owner, final String name, final MethodTypeDesc descriptor, final boolean isInterface) {
            return new RewriteSingle(opcode, owner, name, this.modifyMethodDescriptor(descriptor), isInterface, isInvokeDynamic);
        }

        private MethodTypeDesc modifyMethodDescriptor(final MethodTypeDesc methodDescriptor) {
            return replaceParameters(methodDescriptor, isEqual(this.oldParamType()), this.newParamType());
        }
    }

    /**
     * Changes a return type to a subtype of the old type. This isn't a compile break as subtypes inherit everything, but it is an ABI break.
     * We just change the return type in the descriptor and move on.
     *
     * @param owners owners of the methods to change
     * @param methodMatcher method matcher to find methods with
     * @param oldReturnType the return type that will be found in bytecode that needs to be transformed
     * @param newReturnType the return type that is valid for existing method
     */
    public record SubTypeReturn(Set<Class<?>> owners, MethodMatcher methodMatcher, ClassDesc oldReturnType, ClassDesc newReturnType) implements FilteredMethodRewriteRule {

        @Override
        public @Nullable Rewrite<?> rewrite(final ClassProcessingContext context, final boolean isInvokeDynamic, final int opcode, final ClassDesc owner, final String name, final MethodTypeDesc descriptor, final boolean isInterface) {
            if (!descriptor.returnType().equals(this.newReturnType())) {
                return new RewriteSingle(opcode, owner, name, this.modifyMethodDescriptor(descriptor), isInterface, isInvokeDynamic);
            }
            return null;
        }

        private MethodTypeDesc modifyMethodDescriptor(final MethodTypeDesc methodDescriptor) {
            return methodDescriptor.changeReturnType(this.newReturnType());
        }
    }
}

package io.papermc.asm.rules.method;

import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Method;
import java.util.Set;

import static io.papermc.asm.util.DescriptorUtils.replaceParameters;
import static java.util.function.Predicate.isEqual;

public final class StaticRewrites {

    public static final ClassDesc OBJECT_DESC = Object.class.describeConstable().orElseThrow();

    private StaticRewrites() {
    }

    // fuzzy rewrites are used to convert *all* bytecode descriptors that match the matcher
    // to replace the target param with Object to let the staticHandler sort out the types

    // Uses the methodMatcher against bytecode from plugins. Any matching descriptors will have the method name/owner/descriptor changed to point towards
    // a generated method. That generated method descriptor will have targeted params replaced with Object (the fuzzy-ness). The generated method will
    // load all the arguments into the stack but after loading a "legacy" argument, it will invoke the staticHandler to convert it to the new type. Then
    // it will call the target with the new param type and return the result.
    public record FuzzyParam(Set<Class<?>> owners, ClassDesc existingType, TargetedMethodMatcher methodMatcher, ClassDesc generatedMethodOwner, Method staticHandler) implements StaticRewrite.Generated.Param {

        @Override
        public MethodTypeDesc modifyMethodDescriptor(final MethodTypeDesc bytecodeDescriptor) {
            // We need to replace the parameters in the bytecode descriptor that match the target with the fuzzy param
            return replaceParameters(bytecodeDescriptor, isEqual(this.methodMatcher().targetType()), OBJECT_DESC);
        }

        @Override
        public MethodTypeDesc computeGeneratedDescriptor(final MethodTypeDesc existing, final Set<Integer> context) {
            // To create the generated method descriptor from the existing (in source) descriptor, we replace the
            // existing type with the fuzzy param
            return replaceParameters(existing, isEqual(this.existingType()), OBJECT_DESC, context);
        }
    }

    // Uses the methodMatcher against bytecode from plugins. Any matching descriptors will have their name/owner changed to point towards a
    // generated method with the same descriptor. As the generated method is loading arguments on the stack to prepare for the call to the actual existing method
    // it will call the staticHandler on all parameters that need to be converted.
    public record DirectParam(Set<Class<?>> owners, ClassDesc existingType, TargetedMethodMatcher methodMatcher, ClassDesc generatedMethodOwner, Method staticHandler) implements StaticRewrite.Generated.Param {

        @Override
        public MethodTypeDesc computeGeneratedDescriptor(final MethodTypeDesc existing, final Set<Integer> context) {
            // To create the generated descriptor, we take the existing descriptor (in source) and replace the existing type with the
            // type matched against
            return replaceParameters(existing, isEqual(this.existingType()), this.methodMatcher().targetType(), context);
        }
    }

    public static StaticRewrite.Generated.Return returnRewrite(final Set<Class<?>> owners, final ClassDesc existingType, final TargetedMethodMatcher methodMatcher, final ClassDesc generatedMethodOwner, final Method staticHandler, final ClassDesc intermediateType, final boolean includeOwnerContext) {
        if (!staticHandler.getReturnType().describeConstable().orElseThrow().equals(methodMatcher.targetType())) {
            throw new IllegalArgumentException("Return type of staticHandler doesn't match target from methodMatcher");
        }
        if (staticHandler.getParameterCount() != (includeOwnerContext ? 2 : 1)) {
            throw new IllegalArgumentException("staticHandler should only have %s parameter of type %s".formatted(includeOwnerContext ? 2 : 1, (includeOwnerContext ? "owner type and " : "") + intermediateType));
        }
        if (!staticHandler.getParameterTypes()[includeOwnerContext ? 1 : 0].describeConstable().orElseThrow().equals(intermediateType)) {
            throw new IllegalArgumentException("staticHandler param type isn't " + intermediateType);
        }
        return new Return(owners, existingType, methodMatcher, generatedMethodOwner, staticHandler, includeOwnerContext);
    }

    // Uses the methodMatcher against bytecode from plugins. Any matching descriptors will have the method name/owner changed to point towards
    // a generated method of the same descriptor. That generated method will call the original method and pass the return value
    // to staticHandler. staticHandler will then convert the object to the plugin bytecode's expected type.
    private record Return(Set<Class<?>> owners, ClassDesc existingType, TargetedMethodMatcher methodMatcher, ClassDesc generatedMethodOwner, Method staticHandler, boolean includeOwnerContext) implements StaticRewrite.Generated.Return {

        @Override
        public MethodTypeDesc computeGeneratedDescriptor(final MethodTypeDesc existing, final Void context) {
            return existing.changeReturnType(this.methodMatcher().targetType());
        }
    }
}

package io.papermc.asm.rules.builder.matcher.method;

import io.papermc.asm.rules.builder.matcher.method.targeted.MethodMatcherPredicate;
import java.lang.constant.MethodTypeDesc;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.jetbrains.annotations.Unmodifiable;

import static io.papermc.asm.util.DescriptorUtils.methodDesc;

public interface MethodMatcher extends MethodMatcherPredicate {

    static MethodMatcherBuilder builder() {
        return new MethodMatcherBuilderImpl();
    }

    static MethodMatcher falseMatcher() {
        return MethodMatcherImpl.FALSE;
    }

    static MethodMatcher single(final String name, final Consumer<MethodMatcherBuilder.MatchBuilder> matchBuilderConsumer) {
        return builder().match(name, matchBuilderConsumer).build();
    }

    static MethodMatcher create(final Collection<String> methodNames, final Predicate<? super MethodTypeDesc> bytecodeDescPredicate) {
        return new MethodMatcherImpl(methodNames, bytecodeDescPredicate, (opcode, isInvokeDynamic) -> true);
    }

    static MethodMatcher create(final Collection<String> methodNames, final Predicate<? super MethodTypeDesc> bytecodeDescPredicate, final OpcodePredicate opcodePredicate) {
        return new MethodMatcherImpl(methodNames, bytecodeDescPredicate, opcodePredicate);
    }

    default boolean matches(final String name, final String descriptor) {
        return this.matches(name, methodDesc(descriptor));
    }

    boolean matches(String name, MethodTypeDesc descriptor);

    @Override
    default boolean matches(final int opcode, final boolean isInvokeDynamic, final String name, final String descriptor) {
        return this.matches(opcode, isInvokeDynamic, name, methodDesc(descriptor));
    }

    boolean matches(int opcode, boolean isInvokeDynamic, String name, MethodTypeDesc descriptor);

    @Unmodifiable Set<String> methodNames();

    Predicate<? super MethodTypeDesc> bytecodeDescPredicate();

    OpcodePredicate opcodePredicate();

    /**
     * Creates a method matcher that matches if either matcher passes.
     *
     * @param other the other matcher
     * @return a new "or" matcher
     */
    MethodMatcher or(final MethodMatcher other);

    /**
     * Creates a method matcher that matches if both matcher pass.
     *
     * @param other the other matcher
     * @return a new "and" matcher
     * @see #and(Predicate)
     */
    MethodMatcher and(final MethodMatcher other);

    /**
     * Creates a method matcher tha matches if both this matcher
     * and the method descriptor predicate pass.
     *
     * @param descPredicate the method descriptor predicate
     * @return a new "and" matcher
     */
    MethodMatcher and(final Predicate<? super MethodTypeDesc> descPredicate);
}

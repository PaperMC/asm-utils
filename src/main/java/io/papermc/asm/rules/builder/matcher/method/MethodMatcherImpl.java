package io.papermc.asm.rules.builder.matcher.method;

import java.lang.constant.MethodTypeDesc;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.jetbrains.annotations.Unmodifiable;

class MethodMatcherImpl implements MethodMatcher {

    static final MethodMatcher FALSE = new MethodMatcherImpl(Collections.emptySet(), $ -> false, ($, $$) -> false);

    private final Set<String> methodNames;
    private final Predicate<? super MethodTypeDesc> descriptorPredicate;
    private final OpcodePredicate opcodePredicate;

    MethodMatcherImpl(final Collection<String> methodNames, final Predicate<? super MethodTypeDesc> bytecodeDescPredicate, final OpcodePredicate opcodePredicate) {
        this.methodNames = Set.copyOf(methodNames);
        this.descriptorPredicate = bytecodeDescPredicate;
        this.opcodePredicate = opcodePredicate;
    }

    @Override
    public boolean matches(final String name, final MethodTypeDesc descriptor) {
        return this.methodNames.contains(name) && this.descriptorPredicate.test(descriptor);
    }

    @Override
    public boolean matches(final int opcode, final boolean isInvokeDynamic, final String name, final MethodTypeDesc descriptor) {
        return this.matches(name, descriptor) && this.opcodePredicate.matches(opcode, isInvokeDynamic); // idk which order is faster
    }

    @Override
    public @Unmodifiable Set<String> methodNames() {
        return this.methodNames;
    }

    @Override
    public Predicate<? super MethodTypeDesc> bytecodeDescPredicate() {
        return this.descriptorPredicate;
    }

    @Override
    public OpcodePredicate opcodePredicate() {
        return this.opcodePredicate;
    }

    @Override
    public MethodMatcher or(final MethodMatcher other) {
        final Set<String> copy = new HashSet<>(this.methodNames);
        copy.addAll(other.methodNames());
        return new MethodMatcherImpl(
            copy,
            (d) -> this.descriptorPredicate.test( d) || other.bytecodeDescPredicate().test(d),
            (o, isInvokeDynamic) -> this.opcodePredicate.matches(o, isInvokeDynamic) || other.opcodePredicate().matches(o, isInvokeDynamic)
        );
    }

    @Override
    public MethodMatcher and(final MethodMatcher other) {
        final Set<String> copy = new HashSet<>(this.methodNames);
        copy.retainAll(other.methodNames());
        return new MethodMatcherImpl(
            copy,
            (d) -> this.descriptorPredicate.test(d) && other.bytecodeDescPredicate().test(d),
            (o, isInvokeDynamic) -> this.opcodePredicate.matches(o, isInvokeDynamic) && other.opcodePredicate().matches(o, isInvokeDynamic)
        );
    }

    @Override
    public MethodMatcher and(final Predicate<? super MethodTypeDesc> descPredicate) {
        return new MethodMatcherImpl(
            this.methodNames,
            (d) -> this.descriptorPredicate.test(d) && descPredicate.test(d),
            this.opcodePredicate
        );
    }
}

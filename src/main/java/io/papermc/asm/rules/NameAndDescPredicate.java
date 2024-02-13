package io.papermc.asm.rules;

@FunctionalInterface
public interface NameAndDescPredicate {

    boolean test(String name, String desc);

    default NameAndDescPredicate and(final NameAndDescPredicate other) {
        return (name, desc) -> this.test(name, desc) && other.test(name, desc);
    }

    default NameAndDescPredicate or(final NameAndDescPredicate other) {
        return (name, desc) -> this.test(name, desc) || other.test(name, desc);
    }
}

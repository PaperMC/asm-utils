package io.papermc.asm.rules.builder.matcher.method.targeted;

@FunctionalInterface
public interface MethodMatcherPredicate {

    boolean matches(int opcode, boolean isInvokeDynamic, String name, String descriptor);

    /**
     * Creates a method matcher that matches if both this matcher
     * and the other matcher pass.
     *
     * @param other the other matcher
     * @return a new "and" matcher
     */
    default MethodMatcherPredicate and(final MethodMatcherPredicate other) {
        return (opcode, isInvokeDynamic, name, descriptor) -> this.matches(opcode, isInvokeDynamic, name, descriptor) && other.matches(opcode, isInvokeDynamic, name, descriptor);
    }

    /**
     * Creates a new method matcher that is inverted.
     *
     * @return a new inverted matcher
     */
    default MethodMatcherPredicate negate() {
        return (opcode, isInvokeDynamic, name, descriptor) -> !this.matches(opcode, isInvokeDynamic, name, descriptor);
    }
}

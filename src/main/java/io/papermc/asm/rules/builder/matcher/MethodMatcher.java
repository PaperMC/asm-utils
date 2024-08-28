package io.papermc.asm.rules.builder.matcher;

import io.papermc.asm.rules.method.StaticRewrite;
import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@FunctionalInterface
public interface MethodMatcher {

    boolean matches(int opcode, boolean isInvokeDynamic, String name, String descriptor);

    default boolean matches(final int opcode, final boolean isInvokeDynamic, final String name, final MethodTypeDesc descriptor) {
        return this.matches(opcode, isInvokeDynamic, name, descriptor.descriptorString());
    }

    /**
     * Creates a method matcher that matches if either matcher passes.
     *
     * @param other the other matcher
     * @return a new "or" matcher
     */
    default MethodMatcher or(final MethodMatcher other) {
        return (opcode, isInvokeDynamic, name, descriptor) -> this.matches(opcode, isInvokeDynamic, name, descriptor) || other.matches(opcode, isInvokeDynamic, name, descriptor);
    }

    /**
     * Creates a method matcher that matches if both matcher pass.
     *
     * @param other the other matcher
     * @return a new "and" matcher
     */
    default MethodMatcher and(final MethodMatcher other) {
        return (opcode, isInvokeDynamic, name, descriptor) -> this.matches(opcode, isInvokeDynamic, name, descriptor) && other.matches(opcode, isInvokeDynamic, name, descriptor);
    }

    /**
     * Creates a method matcher that is the inverse of this matcher.
     *
     * @return the inverse matcher
     */
    default MethodMatcher negate() {
        return (opcode, isInvokeDynamic, name, descriptor) -> !this.matches(opcode, isInvokeDynamic, name, descriptor);
    }

    static Builder builder() {
        return new Builder();
    }

    static TargetedMethodMatcher.Builder targeted() {
        return new TargetedMethodMatcher.Builder();
    }

    final class Builder implements io.papermc.asm.util.Builder<MethodMatcher> {
        private MethodMatcher matcher = (o, isInvokeDynamic, n, d) -> false;

        private Builder() {
        }

        public final class MatchBuilder {

            private final Collection<String> names;
            private Predicate<? super MethodTypeDesc> bytecodeDescPredicate = $ -> false;
            private BiPredicate<Integer, Boolean> opcodePredicate = ($, $$) -> true;

            private MatchBuilder(final Collection<String> names) {
                this.names = names;
            }

            public MatchBuilder type(final MethodType...types) {
                this.opcodePredicate = (o, b) -> Arrays.stream(types).anyMatch(type -> type.matches(o, b));
                return this;
            }

            public MatchBuilder virtual() {
                return this.type(MethodType.VIRTUAL);
            }

            public MatchBuilder statik() {
                return this.type(MethodType.STATIC);
            }

            public Builder desc(final String...descriptors) {
                return this.desc(desc -> Arrays.stream(descriptors).anyMatch(d -> desc.descriptorString().equals(d)));
            }

            public Builder desc(final MethodTypeDesc...descriptors) {
                return this.desc(desc -> Arrays.asList(descriptors).contains(desc));
            }

            public Builder desc(final Predicate<? super MethodTypeDesc> descPredicate) {
                this.bytecodeDescPredicate = descPredicate;
                return this.build();
            }

            public Builder build() {
                Builder.this.matcher = Builder.this.matcher.or((opcode, isInvokeDynamic, name, descriptor) ->
                    this.names.contains(name) && this.bytecodeDescPredicate.test(MethodTypeDesc.ofDescriptor(descriptor)) && this.opcodePredicate.test(opcode, isInvokeDynamic)
                );
                return Builder.this;
            }
        }

        public MatchBuilder ctor() {
            return this.match(StaticRewrite.CONSTRUCTOR_METHOD_NAME);
        }

        public MatchBuilder match(final String name) {
            return this.match(Collections.singleton(name));
        }

        public MatchBuilder match(final String...names) {
            return this.match(Set.of(names));
        }

        public MatchBuilder match(final Collection<String> names) {
            return new MatchBuilder(names);
        }

        // insert new helper methods as needed

        @Override
        public MethodMatcher build() {
            // copy to preserve immutability
            return (opcode, isInvokeDynamic, name, descriptor) -> this.matcher.matches(opcode, isInvokeDynamic, name, descriptor);
        }
    }
}

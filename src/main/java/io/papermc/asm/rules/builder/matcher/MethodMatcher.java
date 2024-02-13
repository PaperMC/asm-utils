package io.papermc.asm.rules.builder.matcher;

import io.papermc.asm.rules.NameAndDescPredicate;
import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public interface MethodMatcher {

    boolean matchesName(String name);

    boolean matches(String name, String descriptor);

    static Builder builder() {
        return new Builder();
    }

    static TargetedMethodMatcher.Builder targeted() {
        return new TargetedMethodMatcher.Builder();
    }

    final class Builder implements io.papermc.asm.util.Builder<MethodMatcher> {
        private Predicate<String> byName = $ -> false;
        private NameAndDescPredicate bytecodeNameAndDesc = (n, d) -> false;

        private Builder() {
        }

        public final class MatchBuilder {

            private final Collection<String> names;
            private Predicate<? super MethodTypeDesc> bytecodeDescPredicate = $ -> false;

            private MatchBuilder(final Collection<String> names) {
                this.names = names;
            }

            public Builder desc(final String...descriptors) {
                return this.desc(desc -> Arrays.stream(descriptors).anyMatch(d -> desc.descriptorString().equals(d)));
            }

            public Builder desc(final Predicate<? super MethodTypeDesc> descPredicate) {
                this.bytecodeDescPredicate = descPredicate;
                return this.build();
            }

            public Builder build() {
                Builder.this.bytecodeNameAndDesc = Builder.this.bytecodeNameAndDesc.or((n, d) -> this.names.contains(n) && this.bytecodeDescPredicate.test(MethodTypeDesc.ofDescriptor(d)));
                Builder.this.byName = Builder.this.byName.or(this.names::contains);
                return Builder.this;
            }
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
            return new MethodMatcherImpl(this.byName, this.bytecodeNameAndDesc);
        }
    }
}

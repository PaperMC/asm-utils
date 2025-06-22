package io.papermc.asm.rules.builder.matcher.method;

import io.papermc.asm.rules.method.StaticRewrite;
import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

class MethodMatcherBuilderImpl implements MethodMatcherBuilder {
    private MethodMatcher matcher = MethodMatcher.falseMatcher();

    MethodMatcherBuilderImpl() {
    }

    @Override
    public MethodMatcher build() {
        return this.matcher;
    }

    @Override
    public MethodMatcherBuilder ctor(final Consumer<MatchBuilder> matchBuilderConsumer) {
        final Consumer<MatchBuilder> ctorConsumer = b -> b.type(MethodType.SPECIAL);
        return this.match(StaticRewrite.CONSTRUCTOR_METHOD_NAME, ctorConsumer.andThen(matchBuilderConsumer));
    }

    @Override
    public MethodMatcherBuilder match(final String name, final Consumer<MatchBuilder> matchBuilderConsumer) {
        return this.match(Collections.singleton(name), matchBuilderConsumer);
    }

    @Override
    public MethodMatcherBuilder match(final Collection<String> names, final Consumer<MatchBuilder> matchBuilderConsumer) {
        final SpecificMatchBuilder matchBuilder = new SpecificMatchBuilder(names);
        matchBuilderConsumer.accept(matchBuilder);
        matchBuilder.apply();
        return this;
    }

    @Override
    public MethodMatcherBuilder desc(final Predicate<? super MethodTypeDesc> descPredicate) {
        this.matcher = this.matcher.and(descPredicate);
        return this;
    }

    final class SpecificMatchBuilder implements MatchBuilder {

        private final Collection<String> methodNames;
        private Predicate<? super MethodTypeDesc> bytecodeDescPredicate = $ -> true;
        private OpcodePredicate opcodePredicate = ($, $$) -> true;

        private SpecificMatchBuilder(final Collection<String> methodNames) {
            this.methodNames = Set.copyOf(methodNames);
        }

        private void apply() {
            MethodMatcherBuilderImpl.this.matcher = MethodMatcherBuilderImpl.this.matcher.or(MethodMatcher.create(
                this.methodNames,
                this.bytecodeDescPredicate,
                this.opcodePredicate
            ));
        }

        @Override
        public MatchBuilder type(final MethodType...types) {
            this.opcodePredicate = (o, b) -> Arrays.stream(types).anyMatch(type -> type.matches(o, b));
            return this;
        }

        @Override
        public MatchBuilder desc(final Predicate<? super MethodTypeDesc> descPredicate) {
            this.bytecodeDescPredicate = descPredicate;
            return this;
        }
    }
}

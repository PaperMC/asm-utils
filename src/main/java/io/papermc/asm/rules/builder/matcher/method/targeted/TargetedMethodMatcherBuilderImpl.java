package io.papermc.asm.rules.builder.matcher.method.targeted;

import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.method.MethodType;
import io.papermc.asm.rules.builder.matcher.method.OpcodePredicate;
import io.papermc.asm.rules.method.StaticRewrite;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

class TargetedMethodMatcherBuilderImpl implements TargetedMethodMatcherBuilder {

    private MethodMatcher matcher = MethodMatcher.falseMatcher();
    private Predicate<MethodTypeDesc> byDesc = $ -> true;
    private @Nullable ClassDesc oldType;

    TargetedMethodMatcherBuilderImpl() {
    }

    @Override
    public TargetedMethodMatcherBuilder ctor() {
        return this.match(StaticRewrite.CONSTRUCTOR_METHOD_NAME, b -> b.type(MethodType.SPECIAL));
    }

    @Override
    public TargetedMethodMatcherBuilder match(final String name, final Consumer<MatchBuilder> matchBuilderConsumer) {
        return this.match(Collections.singleton(name), matchBuilderConsumer);
    }

    @Override
    public TargetedMethodMatcherBuilder match(final Collection<String> names, final Consumer<MatchBuilder> matchBuilderConsumer) {
        final SpecificMatchBuilder matchBuilder = new SpecificMatchBuilder(names);
        matchBuilderConsumer.accept(matchBuilder);
        matchBuilder.apply();
        return this;
    }

    @Override
    public TargetedMethodMatcherBuilder targetParam(final ClassDesc classDesc) {
        if (this.oldType != null) {
            throw new IllegalArgumentException("Targeted type was already set to " + this.oldType);
        }
        this.oldType = classDesc;
        return this.hasParam(classDesc);
    }

    @Override
    public TargetedMethodMatcherBuilder targetReturn(final ClassDesc classDesc) {
        if (this.oldType != null) {
            throw new IllegalArgumentException("Targeted type was already set to " + this.oldType);
        }
        this.oldType = classDesc;
        return this.hasReturn(classDesc);
    }

    @Override
    public TargetedMethodMatcherBuilder desc(final Predicate<? super MethodTypeDesc> descPredicate) {
        this.byDesc = this.byDesc.and(descPredicate);
        return this;
    }

    @Override
    public TargetedMethodMatcher build() {
        if (this.oldType == null) {
            throw new IllegalStateException("Targeted type was not set");
        }
        final MethodMatcher finalMatcher = this.matcher.and(this.byDesc);
        return new TargetedMethodMatcherImpl(finalMatcher, this.oldType);
    }

    final class SpecificMatchBuilder implements TargetedMethodMatcherBuilder.MatchBuilder {

        private final Collection<String> methodNames;
        private Predicate<? super MethodTypeDesc> bytecodeDescPredicate = $ -> true;
        private OpcodePredicate opcodePredicate = ($, $$) -> true;

        private SpecificMatchBuilder(final Collection<String> methodNames) {
            this.methodNames = Set.copyOf(methodNames);
        }

        private void apply() {
            TargetedMethodMatcherBuilderImpl.this.matcher = TargetedMethodMatcherBuilderImpl.this.matcher.or(MethodMatcher.create(
                this.methodNames,
                this.bytecodeDescPredicate,
                this.opcodePredicate
            ));
        }

        @Override
        public TargetedMethodMatcherBuilder.MatchBuilder type(final MethodType... types) {
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

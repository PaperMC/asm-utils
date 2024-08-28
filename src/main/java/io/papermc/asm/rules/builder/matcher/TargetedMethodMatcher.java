package io.papermc.asm.rules.builder.matcher;

import io.papermc.asm.rules.method.StaticRewrite;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static java.util.Objects.requireNonNull;

public interface TargetedMethodMatcher extends MethodMatcher {

    ClassDesc targetType();

    class Builder implements io.papermc.asm.util.Builder<TargetedMethodMatcher> {

        private Predicate<String> byName = $ -> false;
        private Predicate<MethodTypeDesc> byDesc = $ -> false;
        private IntPredicate opcodePredicate = $ -> true;
        private @MonotonicNonNull ClassDesc oldType;

        Builder() {
        }

        public Builder ctor() {
            return this.names(StaticRewrite.CONSTRUCTOR_METHOD_NAME);
        }

        public Builder names(final String... names) {
            return this.names(List.of(names));
        }

        public Builder names(final Collection<String> names) {
            this.byName = this.byName.or(names::contains);
            return this;
        }

        public Builder opcode(final int...opcodes) {
            this.opcodePredicate = o -> Arrays.stream(opcodes).anyMatch(opcode -> opcode == o);
            return this;
        }

        public Builder containsParam(final ClassDesc classDesc) {
            this.oldType = classDesc;
            this.byDesc = d -> d.parameterList().contains(classDesc);
            return this;
        }

        public Builder hasReturn(final ClassDesc classDesc) {
            this.oldType = classDesc;
            this.byDesc = d -> d.returnType().equals(classDesc);
            return this;
        }

        public Builder desc(final Predicate<? super MethodTypeDesc> predicate) {
            this.byDesc = this.byDesc.and(predicate);
            return this;
        }

        @Override
        public TargetedMethodMatcher build() {
            final MethodMatcher matcher = (opcode, isInvokeDynamic, name, descriptor) ->
                this.opcodePredicate.test(opcode) && this.byName.test(name) && this.byDesc.test(MethodTypeDesc.ofDescriptor(descriptor));
            return new TargetedMethodMatcherImpl(matcher, requireNonNull(this.oldType));
        }
    }
}

package io.papermc.asm.rules.builder.matcher.field;

import java.lang.constant.ClassDesc;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.isEqual;

public interface FieldMatcher {

    boolean matchesName(String name);

    boolean matches(String name, String descriptor);

    static Builder builder() {
        return new Builder();
    }

    final class Builder implements io.papermc.asm.util.Builder<FieldMatcher> {

        private Predicate<String> byName = $ -> false;
        private Predicate<? super ClassDesc> byType = $ -> true;

        private Builder() {
        }

        public Builder names(final String... names) {
            return this.names(List.of(names));
        }

        public Builder names(final Collection<String> names) {
            this.byName = this.byName.or(names::contains);
            return this;
        }

        public Builder desc(final ClassDesc desc) {
            return this.desc(isEqual(desc));
        }

        public Builder desc(final Predicate<? super ClassDesc> predicate) {
            this.byType = predicate;
            return this;
        }

        @Override
        public FieldMatcher build() {
            return new FieldMatcherImpl(this.byName, this.byType);
        }
    }
}

package io.papermc.classfile.method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static io.papermc.classfile.ClassfileUtils.startsWith;

public sealed interface MethodNamePredicate extends Predicate<CharSequence> {

    static MethodNamePredicate anyNonConstructors() {
        final class Holder {
            static final MethodNamePredicate INSTANCE = new Any();
        }
        return Holder.INSTANCE;
    }

    static MethodNamePredicate constructor() {
        return exact("<init>");
    }

    static MethodNamePredicate exact(final String name, final String... otherNames) {
        final List<String> names = new ArrayList<>();
        names.add(name);
        names.addAll(List.of(otherNames));
        return exact(names);
    }

    static MethodNamePredicate exact(final Collection<String> names) {
        return new ExactMatch(new ArrayList<>(names));
    }

    static MethodNamePredicate prefix(final String prefix) {
        return new PrefixMatch(prefix.toCharArray());
    }

    record ExactMatch(List<String> names) implements MethodNamePredicate {

        public ExactMatch {
            names = List.copyOf(names);
        }

        @Override
        public boolean test(final CharSequence s) {
            return this.names.stream().anyMatch(name -> name.contentEquals(s));
        }
    }

    record PrefixMatch(char[] prefix) implements MethodNamePredicate {

        @Override
        public boolean test(final CharSequence s) {
            return startsWith(s, this.prefix);
        }
    }

    record Any() implements MethodNamePredicate {

        @Override
        public boolean test(final CharSequence charSequence) {
            return true;
        }
    }
}

package io.papermc.classfile.method;

import io.papermc.classfile.ClassFiles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public sealed interface MethodNamePredicate extends Predicate<String> {

    static MethodNamePredicate constructor() {
        final class Holder {
            static final MethodNamePredicate INSTANCE = new Constructor();
        }
        return Holder.INSTANCE;
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
        return new PrefixMatch(prefix);
    }

    record ExactMatch(List<String> names) implements MethodNamePredicate {

        public ExactMatch {
            if (names.stream().anyMatch(s -> s.equals(ClassFiles.CONSTRUCTOR_METHOD_NAME))) {
                throw new IllegalArgumentException("Cannot use <init> as a method name, use the dedicated constructor predicate");
            }
            names = List.copyOf(names);
        }

        @Override
        public boolean test(final String s) {
            return this.names.stream().anyMatch(s::equals);
        }
    }

    record Constructor() implements MethodNamePredicate {

        @Override
        public boolean test(final String charSequence) {
            return ClassFiles.CONSTRUCTOR_METHOD_NAME.equals(charSequence);
        }
    }

    record PrefixMatch(String prefix) implements MethodNamePredicate {

        public PrefixMatch {
            if (ClassFiles.CONSTRUCTOR_METHOD_NAME.startsWith(prefix)) {
                throw new IllegalArgumentException("Cannot use <init> as a method name, use the dedicated constructor predicate");
            }
        }

        @Override
        public boolean test(final String s) {
            return s.startsWith(this.prefix);
        }
    }
}

package io.papermc.classfile.method;

import io.papermc.classfile.ClassFiles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static io.papermc.classfile.ClassFiles.startsWith;

public sealed interface MethodNamePredicate extends Predicate<CharSequence> {

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
        return new PrefixMatch(prefix.toCharArray());
    }

    record ExactMatch(List<String> names) implements MethodNamePredicate {

        public ExactMatch {
            if (names.stream().anyMatch(s -> s.equals(ClassFiles.CONSTRUCTOR_METHOD_NAME))) {
                throw new IllegalArgumentException("Cannot use <init> as a method name, use the dedicated construtor predicate");
            }
            names = List.copyOf(names);
        }

        @Override
        public boolean test(final CharSequence s) {
            return this.names.stream().anyMatch(name -> name.contentEquals(s));
        }
    }

    record Constructor() implements MethodNamePredicate {

        @Override
        public boolean test(final CharSequence charSequence) {
            return ClassFiles.CONSTRUCTOR_METHOD_NAME.contentEquals(charSequence);
        }
    }

    record PrefixMatch(char[] prefix) implements MethodNamePredicate {

        public PrefixMatch {
            if (ClassFiles.startsWith(ClassFiles.CONSTRUCTOR_METHOD_NAME, prefix)) {
                throw new IllegalArgumentException("Cannot use <init> as a method name, use the dedicated construtor predicate");
            }
        }

        @Override
        public boolean test(final CharSequence s) {
            return startsWith(s, this.prefix);
        }
    }
}

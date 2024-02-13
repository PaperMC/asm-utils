package io.papermc.asm.rules.builder.matcher;

import io.papermc.asm.rules.NameAndDescPredicate;
import java.util.function.Predicate;

public class MethodMatcherImpl implements MethodMatcher {

    private final Predicate<String> byName;
    private final NameAndDescPredicate bytecodeNameAndDesc;

    MethodMatcherImpl(final Predicate<String> byName, final NameAndDescPredicate bytecodeNameAndDesc) {
        this.byName = byName;
        this.bytecodeNameAndDesc = bytecodeNameAndDesc;
    }

    @Override
    public boolean matchesName(final String name) {
        return this.byName.test(name);
    }

    @Override
    public boolean matches(final String name, final String descriptor) {
        return this.bytecodeNameAndDesc.test(name, descriptor);
    }
}

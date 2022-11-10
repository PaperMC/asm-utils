package io.papermc.reflectionrewriter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class MethodMatcher {
    public final Set<String> anyDesc = new HashSet<>();
    public final Set<String> nameAndDesc = new HashSet<>();

    public MethodMatcher matchAnyDesc(final String name) {
        this.anyDesc.add(name);
        return this;
    }

    public MethodMatcher match(final String name, final String desc) {
        this.nameAndDesc.add(name + desc);
        return this;
    }

    public MethodMatcher match(final String name, final List<String> descs) {
        for (final String s : descs) {
            this.match(name, s);
        }
        return this;
    }

    public MethodMatcher match(final List<String> names, final String desc) {
        for (final String name : names) {
            this.match(name, desc);
        }
        return this;
    }

    public boolean matches(final String name, final String descriptor) {
        if (this.anyDesc.contains(name)) {
            return true;
        }
        return this.nameAndDesc.contains(name + descriptor);
    }
}

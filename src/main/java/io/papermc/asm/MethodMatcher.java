package io.papermc.asm;

import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class MethodMatcher {
    private final Set<String> anyDesc;
    private final Set<String> anyName;
    private final Set<String> nameAndDesc;

    private MethodMatcher(final Set<String> anyDesc, final Set<String> anyName, final Set<String> nameAndDesc) {
        this.anyDesc = Set.copyOf(anyDesc);
        this.anyName = Set.copyOf(anyName);
        this.nameAndDesc = Set.copyOf(nameAndDesc);
    }

    public boolean matches(final String name, final String descriptor) {
        return this.anyName.contains(descriptor)
            || this.anyDesc.contains(name)
            || this.nameAndDesc.contains(name + descriptor);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Set<String> anyDesc = new HashSet<>();
        private final Set<String> anyName = new HashSet<>();
        private final Set<String> nameAndDesc = new HashSet<>();

        private Builder() {
        }

        public Builder matchAnyName(final String desc) {
            this.anyName.add(desc);
            return this;
        }

        public Builder matchAnyDesc(final String name) {
            this.anyDesc.add(name);
            return this;
        }

        public Builder match(final String name, final String desc) {
            this.nameAndDesc.add(name + desc);
            return this;
        }

        public Builder match(final String name, final Set<String> descs) {
            for (final String s : descs) {
                this.match(name, s);
            }
            return this;
        }

        public Builder match(final Set<String> names, final String desc) {
            for (final String name : names) {
                this.match(name, desc);
            }
            return this;
        }

        public Builder match(final Set<String> names, final Set<String> descs) {
            for (final String name : names) {
                this.match(name, descs);
            }
            return this;
        }

        public MethodMatcher build() {
            return new MethodMatcher(this.anyDesc, this.anyName, this.nameAndDesc);
        }
    }
}

package io.papermc.asm;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public record TestApiVersionImpl(int version) implements io.papermc.asm.versioned.ApiVersion<TestApiVersionImpl> {

    @Override
    public int compareTo(final TestApiVersionImpl o) {
        return Integer.compare(this.version, o.version);
    }
}

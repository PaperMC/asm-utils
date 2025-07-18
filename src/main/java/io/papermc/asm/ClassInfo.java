package io.papermc.asm;

import org.jspecify.annotations.Nullable;

public interface ClassInfo {
    String name();

    boolean isEnum();

    @Nullable String superClassName();

    static ClassInfo create(
        final String name,
        final boolean isEnum,
        final @Nullable String superClassName
    ) {
        return new ClassInfoImpl(name, isEnum, superClassName);
    }
}

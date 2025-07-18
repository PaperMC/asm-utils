package io.papermc.asm;

import org.jspecify.annotations.Nullable;

record ClassInfoImpl(
    String name,
    boolean isEnum,
    @Nullable String superClassName
) implements ClassInfo {
}

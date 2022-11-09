package io.papermc.reflectionrewriter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public record ClassInfo(String name, boolean isEnum, @Nullable String superClass) {
}

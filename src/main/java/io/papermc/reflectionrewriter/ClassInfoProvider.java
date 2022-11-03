package io.papermc.reflectionrewriter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@FunctionalInterface
public interface ClassInfoProvider {
    @Nullable ClassInfo info(String className);

    static ClassInfoProvider basic() {
        return className -> {
            try {
                // Note: Will not work if the class is not already on the classpath, most likely don't want to use this
                final Class<?> clazz = Class.forName(className.replace("/", "."));
                return new ClassInfo(className, clazz.isEnum());
            } catch (final ClassNotFoundException ex) {
                return null;
            }
        };
    }
}

package io.papermc.asm;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface ClassProcessingContext {
    ClassInfoProvider classInfoProvider();

    String processingClassName();

    @Nullable String processingClassSuperClassName();

    static ClassProcessingContext create(
        final ClassInfoProvider classInfoProvider,
        final String processingClassName,
        final @Nullable String processingClassSuperClassName
    ) {
        return new ClassProcessingContext() {
            @Override
            public ClassInfoProvider classInfoProvider() {
                return classInfoProvider;
            }

            @Override
            public String processingClassName() {
                return processingClassName;
            }

            @Override
            public @Nullable String processingClassSuperClassName() {
                return processingClassSuperClassName;
            }
        };
    }
}

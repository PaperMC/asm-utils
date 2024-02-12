package io.papermc.asmutils;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@FunctionalInterface
public interface ShouldProcess {
    boolean shouldProcess(ClassProcessingContext context);
}

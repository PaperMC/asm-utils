package io.papermc.asm.rules.classes;

import java.util.Optional;

/**
 * This type needs to be implemented by the implementation of the
 * type that was previously an enum and is now an interface.
 *
 * <p>
 * Types need to have static methods for {@code values()}
 * and {@code valueOf(String)}.
 * </p>
 *
 * @param <T> the implementation type
 */
public interface LegacyEnum<T extends LegacyEnum<T>> extends Comparable<T> {

    String name();

    int ordinal();

    @SuppressWarnings({"unchecked", "MethodName"})
    default Class<T> getDeclaringClass() {
        return (Class<T>) this.getClass();
    }

    default Optional<Enum.EnumDesc<?>> describeConstable() {
        return this.getDeclaringClass().describeConstable().map(c -> Enum.EnumDesc.of(c, this.name()));
    }
}

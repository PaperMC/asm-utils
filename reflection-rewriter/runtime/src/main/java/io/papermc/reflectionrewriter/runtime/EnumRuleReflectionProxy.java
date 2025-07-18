package io.papermc.reflectionrewriter.runtime;

import java.lang.invoke.MethodHandles;

/**
 * Has required methods for the non-default enum rule. Implement in addition to
 * {@link DefaultRulesReflectionProxy} if using the enum rule.
 */
public interface EnumRuleReflectionProxy {
    <E extends Enum<E>> E enumConstant(MethodHandles.Lookup lookup, String name, Class<E> type);

    <E extends Enum<E>> E valueOf(Class<E> enumClass, String name);

    default <E extends Enum<E>> E valueOf(final String name, final Class<E> enumClass) {
        return this.valueOf(enumClass, name);
    }
}

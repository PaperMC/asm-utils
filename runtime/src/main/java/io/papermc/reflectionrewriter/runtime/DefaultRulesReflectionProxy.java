package io.papermc.reflectionrewriter.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Interface with all the methods needed for the default rules to function.
 *
 * <p>Since the proxy methods must all be static, ProxyGenerator
 * should be used to generate a class with a static field holding an instance
 * of your proxy, and static methods redirecting to that instance.</p>
 */
@DefaultQualifier(NonNull.class)
@SuppressWarnings("checkstyle:MethodName") // These methods have their names copied from the method we are proxying
public interface DefaultRulesReflectionProxy {
    // Begin standard reflection
    Class<?> forName(String name) throws ClassNotFoundException;

    Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException;

    Class<?> forName(Module module, String name);

    Field getField(Class<?> clazz, String name) throws NoSuchFieldException, SecurityException;

    Field getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException, SecurityException;

    Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException;

    Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException;
    // End standard reflection

    // Begin MethodHandles
    MethodHandle findStatic(MethodHandles.Lookup lookup, Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException;

    MethodHandle findVirtual(MethodHandles.Lookup lookup, Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException;

    Class<?> findClass(MethodHandles.Lookup lookup, String targetName) throws ClassNotFoundException, IllegalAccessException;

    MethodHandle findSpecial(MethodHandles.Lookup lookup, Class<?> refc, String name, MethodType type, Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException;

    MethodHandle findGetter(MethodHandles.Lookup lookup, Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException;

    MethodHandle findSetter(MethodHandles.Lookup lookup, Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException;

    MethodHandle findStaticGetter(MethodHandles.Lookup lookup, Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException;

    MethodHandle findStaticSetter(MethodHandles.Lookup lookup, Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException;
    // End MethodHandles

    // Begin LambdaMetafactory
    CallSite metafactory(MethodHandles.Lookup caller, String interfaceMethodName, MethodType factoryType, MethodType interfaceMethodType, MethodHandle implementation, MethodType dynamicMethodType) throws LambdaConversionException;

    CallSite altMetafactory(MethodHandles.Lookup caller, String interfaceMethodName, MethodType factoryType, Object... args) throws LambdaConversionException;
    // End LambdaMetafactory

    // Begin ConstantBootstraps
    Object getStaticFinal(MethodHandles.Lookup lookup, String name, Class<?> type, Class<?> declaringClass);

    Object getStaticFinal(MethodHandles.Lookup lookup, String name, Class<?> type);
    // End ConstantBootstraps

    // Begin MethodType
    MethodType fromMethodDescriptorString(String descriptor, ClassLoader loader) throws IllegalArgumentException, TypeNotPresentException;
    // End MethodType

    /**
     * Has required methods for the non-default enum rule. Implement in addition to
     * {@link DefaultRulesReflectionProxy} if using the enum rule.
     */
    interface EnumRuleReflectionProxy {
        <E extends Enum<E>> E enumConstant(MethodHandles.Lookup lookup, String name, Class<E> type);

        <E extends java.lang.Enum<E>> E valueOf(Class<E> enumClass, String name);

        default <E extends java.lang.Enum<E>> E valueOf(final String name, final Class<E> enumClass) {
            return this.valueOf(enumClass, name);
        }
    }
}

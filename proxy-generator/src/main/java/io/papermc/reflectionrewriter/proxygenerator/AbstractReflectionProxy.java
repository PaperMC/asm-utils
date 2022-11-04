package io.papermc.reflectionrewriter.proxygenerator;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantBootstraps;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.Type;

@DefaultQualifier(NonNull.class)
public abstract class AbstractReflectionProxy implements ReflectionProxy {
    protected AbstractReflectionProxy() {
    }

    protected abstract String mapClassName(String name);

    protected abstract String mapDeclaredMethodName(Class<?> clazz, String name, Class<?>... parameterTypes);

    protected abstract String mapMethodName(Class<?> clazz, String name, Class<?>... parameterTypes);

    protected abstract String mapDeclaredFieldName(Class<?> clazz, String name);

    protected abstract String mapFieldName(Class<?> clazz, String name);

    protected final String mapClassOrArrayName(final String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            return name;
        }

        // Array type
        if (name.charAt(0) == '[') {
            final int last = name.lastIndexOf('[');

            try {
                // Object array
                if (name.charAt(last + 1) == 'L') {
                    final String cls = name.substring(last + 2, name.length() - 1);
                    return name.substring(0, last + 2) + this.mapClassName(cls) + ';';
                }
            } catch (final Exception ex) {
                // Pass through on invalid names
                return name;
            }

            // Primitive array
            return name;
        }

        return this.mapClassName(name);
    }

    // todo: rewrite this method & fromMethodDescriptorString without using ASM
    protected final Class<?> mappedJavaType(
        final Type type,
        final ClassLoader loader
    ) throws TypeNotPresentException {
        return switch (type.getSort()) {
            case Type.ARRAY -> {
                final String arrPrefix = "[".repeat(type.getDimensions());
                final String typeName = type.getElementType().getSort() == Type.OBJECT
                    ? 'L' + type.getElementType().getClassName() + ';'
                    : type.getElementType().getDescriptor();
                yield this.forNameTypeNotPresentException(arrPrefix + typeName, loader);
            }
            case Type.OBJECT -> this.forNameTypeNotPresentException(type.getClassName(), loader);
            case Type.VOID -> Void.TYPE;
            case Type.INT -> int.class;
            case Type.BOOLEAN -> boolean.class;
            case Type.BYTE -> byte.class;
            case Type.CHAR -> char.class;
            case Type.SHORT -> short.class;
            case Type.DOUBLE -> double.class;
            case Type.FLOAT -> float.class;
            case Type.LONG -> long.class;
            default -> throw new IllegalArgumentException();
        };
    }

    protected final Class<?> forNameTypeNotPresentException(final String name, final ClassLoader loader) {
        try {
            return this.forName(name, false, loader);
        } catch (final ClassNotFoundException ex) {
            throw new TypeNotPresentException(name, ex);
        }
    }

    private static ClassLoader callerClassLoader() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(3).findFirst().map(frame -> frame.getDeclaringClass().getClassLoader()).orElseThrow());
    }

    // Begin standard reflection
    @Override
    public Class<?> forName(final String name) throws ClassNotFoundException {
        return Class.forName(this.mapClassOrArrayName(name), true, callerClassLoader());
    }

    @Override
    public Class<?> forName(final String name, final boolean initialize, final ClassLoader loader) throws ClassNotFoundException {
        return Class.forName(this.mapClassOrArrayName(name), initialize, loader);
    }

    @Override
    public Class<?> forName(final Module module, final String name) {
        // Uses the module's class loader unless a SecurityManager is used
        // todo pass calling classloader when SecurityManager is used?
        return Class.forName(module, this.mapClassOrArrayName(name));
    }

    @Override
    public Field getField(final Class<?> clazz, final String name) throws NoSuchFieldException, SecurityException {
        return clazz.getField(this.mapFieldName(clazz, name));
    }

    @Override
    public Field getDeclaredField(final Class<?> clazz, final String name) throws NoSuchFieldException, SecurityException {
        return clazz.getDeclaredField(this.mapDeclaredFieldName(clazz, name));
    }

    @Override
    public Method getDeclaredMethod(final Class<?> clazz, final String name, final Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return clazz.getDeclaredMethod(this.mapDeclaredMethodName(clazz, name, parameterTypes), parameterTypes);
    }

    @Override
    public Method getMethod(final Class<?> clazz, final String name, final Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return clazz.getMethod(this.mapMethodName(clazz, name, parameterTypes), parameterTypes);
    }
    // End standard reflection

    // Begin MethodHandles
    @Override
    public MethodHandle findStatic(final MethodHandles.Lookup lookup, final Class<?> refc, final String name, final MethodType type) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findStatic(refc, this.mapMethodName(refc, name, type.parameterArray()), type);
    }

    @Override
    public MethodHandle findVirtual(final MethodHandles.Lookup lookup, final Class<?> refc, final String name, final MethodType type) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findVirtual(refc, this.mapMethodName(refc, name, type.parameterArray()), type);
    }

    @Override
    public Class<?> findClass(final MethodHandles.Lookup lookup, final String targetName) throws ClassNotFoundException, IllegalAccessException {
        return lookup.findClass(this.mapClassOrArrayName(targetName));
    }

    @Override
    public MethodHandle findSpecial(final MethodHandles.Lookup lookup, final Class<?> refc, final String name, final MethodType type, final Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findSpecial(refc, this.mapMethodName(refc, name, type.parameterArray()), type, specialCaller);
    }

    @Override
    public MethodHandle findGetter(final MethodHandles.Lookup lookup, final Class<?> refc, final String name, final Class<?> type) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findGetter(refc, this.mapFieldName(refc, name), type);
    }

    @Override
    public MethodHandle findSetter(final MethodHandles.Lookup lookup, final Class<?> refc, final String name, final Class<?> type) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findSetter(refc, this.mapFieldName(refc, name), type);
    }

    @Override
    public MethodHandle findStaticGetter(final MethodHandles.Lookup lookup, final Class<?> refc, final String name, final Class<?> type) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findStaticGetter(refc, this.mapFieldName(refc, name), type);
    }

    @Override
    public MethodHandle findStaticSetter(final MethodHandles.Lookup lookup, final Class<?> refc, final String name, final Class<?> type) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findStaticSetter(refc, this.mapFieldName(refc, name), type);
    }
    // End MethodHandles

    // Begin LambdaMetafactory
    @Override
    public CallSite metafactory(final MethodHandles.Lookup caller, final String interfaceMethodName, final MethodType factoryType, final MethodType interfaceMethodType, final MethodHandle implementation, final MethodType dynamicMethodType) throws LambdaConversionException {
        return LambdaMetafactory.metafactory(caller, this.mapMethodName(factoryType.returnType(), interfaceMethodName, dynamicMethodType.parameterArray()), factoryType, interfaceMethodType, implementation, dynamicMethodType);
    }

    @Override
    public CallSite altMetafactory(final MethodHandles.Lookup caller, final String interfaceMethodName, final MethodType factoryType, final Object... args) throws LambdaConversionException {
        if (args.length < 3 || !(args[2] instanceof MethodType dynamicMethodType)) {
            throw new IllegalArgumentException("illegal or missing argument");
        }
        return LambdaMetafactory.altMetafactory(caller, this.mapMethodName(factoryType.returnType(), interfaceMethodName, dynamicMethodType.parameterArray()), factoryType, args);
    }
    // End LambdaMetafactory

    // Begin ConstantBootstraps
    @Override
    public Object getStaticFinal(final MethodHandles.Lookup lookup, final String name, final Class<?> type, final Class<?> declaringClass) {
        return ConstantBootstraps.getStaticFinal(lookup, this.mapFieldName(declaringClass, name), type, declaringClass);
    }

    @Override
    public Object getStaticFinal(final MethodHandles.Lookup lookup, final String name, final Class<?> type) {
        return ConstantBootstraps.getStaticFinal(lookup, this.mapFieldName(type, name), type);
    }
    // End ConstantBootstraps

    // Begin MethodType
    @Override
    public MethodType fromMethodDescriptorString(final String descriptor, final ClassLoader loader) throws IllegalArgumentException, TypeNotPresentException {
        if (!descriptor.startsWith("(") || // also generates NPE if needed
            descriptor.indexOf(')') < 0 ||
            descriptor.indexOf('.') >= 0) {
            throw new IllegalArgumentException("Not a valid method descriptor: " + descriptor);
        }
        final Type methodType = Type.getType(descriptor);
        final List<Class<?>> params = new ArrayList<>();
        for (final Type t : methodType.getArgumentTypes()) {
            params.add(this.mappedJavaType(t, loader));
        }
        return MethodType.methodType(this.mappedJavaType(methodType.getReturnType(), loader), params);
    }
    // End MethodType
}

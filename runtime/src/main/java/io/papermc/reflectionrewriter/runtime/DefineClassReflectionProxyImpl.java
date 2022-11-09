package io.papermc.reflectionrewriter.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

final class DefineClassReflectionProxyImpl implements DefineClassReflectionProxy {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Map<Class<?>, MethodHandle> handles = new ConcurrentHashMap<>();
    private final Function<byte[], byte[]> classTransformer;

    DefineClassReflectionProxyImpl(final Function<byte[], byte[]> classTransformer) {
        this.classTransformer = classTransformer;
    }

    @Override
    public Class<?> defineClass(final ClassLoader loader, final byte[] b, final int off, final int len) throws ClassFormatError {
        return this.defineClass(loader, null, b, off, len, null);
    }

    @Override
    public Class<?> defineClass(final ClassLoader loader, final String name, final byte[] b, final int off, final int len) throws ClassFormatError {
        return this.defineClass(loader, name, b, off, len, null);
    }

    @Override
    public Class<?> defineClass(final ClassLoader loader, final @Nullable String name, final byte[] b, final int off, final int len, final @Nullable ProtectionDomain protectionDomain) throws ClassFormatError {
        try {
            final byte[] bytes = new byte[len];
            System.arraycopy(b, off, bytes, 0, len);
            final MethodHandle handle = this.handles.computeIfAbsent(loader.getClass(), clazz -> {
                try {
                    return MethodHandles.privateLookupIn(clazz, LOOKUP).findVirtual(
                        ClassLoader.class,
                        "defineClass",
                        MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class)
                    ).asType(
                        MethodType.methodType(Class.class, ClassLoader.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class)
                    );
                } catch (final ReflectiveOperationException ex) {
                    throw new RuntimeException("Failed to lookup defineClass handle", ex);
                }
            });
            final byte[] transformed = this.classTransformer.apply(bytes);
            return (Class<?>) handle.invokeExact(loader, name, transformed, 0, transformed.length, protectionDomain);
        } catch (final Error error) {
            throw error;
        } catch (final Throwable ex) {
            throw new RuntimeException("Failed to invoke defineClass", ex);
        }
    }

    @Override
    public Class<?> defineClass(final ClassLoader loader, final String name, final ByteBuffer b, final ProtectionDomain protectionDomain) throws ClassFormatError {
        final int len = b.remaining();
        if (b.hasArray()) {
            return this.defineClass(loader, name, b.array(), b.position() + b.arrayOffset(), len, protectionDomain);
        } else {
            // no array, or read-only array
            final byte[] tb = new byte[len];
            b.get(tb); // get bytes out of byte buffer.
            return this.defineClass(loader, name, tb, 0, len, protectionDomain);
        }
    }

    @Override
    public Class<?> defineClass(final MethodHandles.Lookup lookup, final byte[] bytes) throws IllegalAccessException {
        return lookup.defineClass(this.classTransformer.apply(bytes));
    }
}

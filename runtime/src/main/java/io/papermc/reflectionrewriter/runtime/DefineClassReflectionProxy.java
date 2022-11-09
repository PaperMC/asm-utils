package io.papermc.reflectionrewriter.runtime;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.function.Function;

public interface DefineClassReflectionProxy {
    // ClassLoader start
    default Class<?> defineClass(final ClassLoader loader, final byte[] b, final int off, final int len) throws ClassFormatError {
        return this.defineClass(loader, null, b, off, len, null);
    }

    default Class<?> defineClass(final ClassLoader loader, final String name, final byte[] b, final int off, final int len) throws ClassFormatError {
        return this.defineClass(loader, name, b, off, len, null);
    }

    Class<?> defineClass(ClassLoader loader, String name, byte[] b, int off, int len, ProtectionDomain protectionDomain) throws ClassFormatError;

    default Class<?> defineClass(final ClassLoader loader, final String name, final ByteBuffer b, final ProtectionDomain protectionDomain) throws ClassFormatError {
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
    // ClassLoader end

    // MethodHandles.Lookup start
    Class<?> defineClass(MethodHandles.Lookup lookup, byte[] bytes) throws IllegalAccessException;
    // MethodHandles.Lookup end

    static DefineClassReflectionProxy create(final Function<byte[], byte[]> classTransformer) {
        return new DefineClassReflectionProxyImpl(classTransformer);
    }
}

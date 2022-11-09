package io.papermc.reflectionrewriter.runtime;

import java.lang.invoke.MethodHandles;
import java.security.ProtectionDomain;
import java.util.function.Function;

public interface DefineClassReflectionProxy {
    // ClassLoader start
    Class<?> defineClass(ClassLoader loader, byte[] b, int off, int len) throws ClassFormatError;

    Class<?> defineClass(ClassLoader loader, String name, byte[] b, int off, int len) throws ClassFormatError;

    Class<?> defineClass(ClassLoader loader, String name, byte[] b, int off, int len, ProtectionDomain protectionDomain) throws ClassFormatError;

    Class<?> defineClass(ClassLoader loader, String name, java.nio.ByteBuffer b, ProtectionDomain protectionDomain) throws ClassFormatError;
    // ClassLoader end

    // MethodHandles.Lookup start
    Class<?> defineClass(MethodHandles.Lookup lookup, byte[] bytes) throws IllegalAccessException;
    // MethodHandles.Lookup end

    static DefineClassReflectionProxy create(final Function<byte[], byte[]> classTransformer) {
        return new DefineClassReflectionProxyImpl(classTransformer);
    }
}

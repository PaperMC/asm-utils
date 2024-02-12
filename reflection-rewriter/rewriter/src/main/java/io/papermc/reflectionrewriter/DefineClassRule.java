package io.papermc.reflectionrewriter;

import io.papermc.asmutils.ClassInfo;
import io.papermc.asmutils.ClassInfoProvider;
import io.papermc.asmutils.ClassProcessingContext;
import io.papermc.asmutils.InvokeStaticRewrite;
import io.papermc.asmutils.RewriteRule;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.Opcodes;

@DefaultQualifier(NonNull.class)
public final class DefineClassRule implements InvokeStaticRewrite {
    private static final Set<String> CLASS_LOADER_DESCS = Set.of(
        "([BII)Ljava/lang/Class;",
        "(Ljava/lang/String;[BII)Ljava/lang/Class;",
        "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;)Ljava/lang/Class;",
        "(Ljava/lang/String;Ljava/nio/ByteBuffer;Ljava/security/ProtectionDomain;)Ljava/lang/Class;"
    );
    private static final Set<String> SECURE_CLASS_LOADER_DESCS = Set.of(
        "(Ljava/lang/String;Ljava/nio/ByteBuffer;Ljava/security/CodeSource;)Ljava/lang/Class;",
        "(Ljava/lang/String;[BIILjava/security/CodeSource;)Ljava/lang/Class;"
    );

    private final String proxy;
    private final boolean assumeClassLoader;

    private DefineClassRule(final String proxyClassName, final boolean assumeClassLoader) {
        this.proxy = proxyClassName;
        this.assumeClassLoader = assumeClassLoader;
    }

    // We could split this into multiple rules and return false for shouldProcess when the processing class doesn't
    // extend (S)CL. However since the MethodHandles.Lookup portion always needs to run, the actual benefit would
    // be beyond minute (if not actually worse).
    @Override
    public @Nullable Rewrite rewrite(
        final ClassProcessingContext context,
        final int opcode,
        final String owner,
        final String name,
        final String descriptor,
        final boolean isInterface
    ) {
        if (!name.equals("defineClass") || opcode == Opcodes.INVOKESTATIC || opcode == Opcodes.H_INVOKESTATIC) {
            return null;
        }
        if (owner.equals("java/lang/invoke/MethodHandles$Lookup") && descriptor.equals("([B)Ljava/lang/Class;")) {
            return InvokeStaticRewrite.staticRedirect(this.proxy, name, InvokeStaticRewrite.insertFirstParam("java/lang/invoke/MethodHandles$Lookup", descriptor));
        }
        final @Nullable String superName = context.processingClassSuperClassName();
        if (superName != null) {
            if (CLASS_LOADER_DESCS.contains(descriptor)
                && this.isClassLoader(context.classInfoProvider(), superName)
                && (owner.equals(context.processingClassName()) || this.isClassLoader(context.classInfoProvider(), owner))) {
                return this.classLoaderRewrite(name, descriptor);
            } else if (SECURE_CLASS_LOADER_DESCS.contains(descriptor)
                && this.isSecureClassLoader(context.classInfoProvider(), superName)
                && (owner.equals(context.processingClassName()) || this.isSecureClassLoader(context.classInfoProvider(), owner))) {
                return this.classLoaderRewrite(name, descriptor);
            }
        }
        return null;
    }

    private Rewrite classLoaderRewrite(final String name, final String descriptor) {
        return InvokeStaticRewrite.staticRedirect(this.proxy, name, InvokeStaticRewrite.insertFirstParam("java/lang/Object", descriptor));
    }

    private boolean isSecureClassLoader(final ClassInfoProvider classInfoProvider, final String className) {
        return is(classInfoProvider, className, "java/security/SecureClassLoader", this.assumeClassLoader);
    }

    private boolean isClassLoader(final ClassInfoProvider classInfoProvider, final String className) {
        return is(classInfoProvider, className, "java/lang/ClassLoader", this.assumeClassLoader);
    }

    /**
     * Create a rewrite rule for MethodHandles.Lookup#defineClass and (Secure)ClassLoader#defineClass.
     *
     * @param proxyClassName proxy class name
     * @return new rule
     */
    public static RewriteRule create(final String proxyClassName) {
        return create(proxyClassName, false);
    }

    /**
     * Create a rewrite rule for MethodHandles.Lookup#defineClass and (Secure)ClassLoader#defineClass.
     *
     * @param proxyClassName    proxy class name
     * @param assumeClassLoader whether to assume a class is a {@link ClassLoader} if it cannot be determined
     *                          using the {@link ClassInfoProvider}
     * @return new rule
     */
    public static RewriteRule create(final String proxyClassName, final boolean assumeClassLoader) {
        return RewriteRule.create(new DefineClassRule(proxyClassName, assumeClassLoader));
    }

    private static boolean is(final ClassInfoProvider classInfoProvider, final String className, final String checkForName, final boolean assumeClassLoader) {
        if (className.equals(checkForName)) {
            return true;
        }
        final @Nullable ClassInfo info = classInfoProvider.info(className);
        if (info != null) {
            final @Nullable String superName = info.superClassName();
            if (superName != null) {
                return is(classInfoProvider, superName, checkForName, assumeClassLoader);
            } else {
                return false;
            }
        }
        return assumeClassLoader;
    }
}

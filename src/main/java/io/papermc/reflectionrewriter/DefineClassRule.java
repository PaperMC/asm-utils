package io.papermc.reflectionrewriter;

import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.Opcodes;

@DefaultQualifier(NonNull.class)
public final class DefineClassRule {
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

    private DefineClassRule() {
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
     *                          using {@code classInfoProvider}
     * @return new rule
     */
    public static RewriteRule create(final String proxyClassName, final boolean assumeClassLoader) {
        return RewriteRule.create((InvokeStaticRewrite) (classInfoProvider, opcode, owner, name, descriptor, isInterface) -> {
            final boolean staticCall = opcode == Opcodes.INVOKESTATIC || opcode == Opcodes.H_INVOKESTATIC;
            if (!staticCall && name.equals("defineClass") && CLASS_LOADER_DESCS.contains(descriptor) && isClassLoader(classInfoProvider, owner, assumeClassLoader)) {
                final String redirectedDescriptor = "(Ljava/lang/Object;" + descriptor.substring(1);
                return InvokeStaticRewrite.staticRedirect(proxyClassName, name, redirectedDescriptor);
            } else if (!staticCall && name.equals("defineClass") && SECURE_CLASS_LOADER_DESCS.contains(descriptor) && isSecureClassLoader(classInfoProvider, owner, assumeClassLoader)) {
                final String redirectedDescriptor = "(Ljava/lang/Object;" + descriptor.substring(1);
                return InvokeStaticRewrite.staticRedirect(proxyClassName, name, redirectedDescriptor);
            } else if (owner.equals("java/lang/invoke/MethodHandles$Lookup") && name.equals("defineClass") && descriptor.equals("([B)Ljava/lang/Class;")) {
                final String redirectedDescriptor = "(Ljava/lang/invoke/MethodHandles$Lookup;" + descriptor.substring(1);
                return InvokeStaticRewrite.staticRedirect(proxyClassName, name, redirectedDescriptor);
            }
            return null;
        });
    }

    private static boolean isSecureClassLoader(final ClassInfoProvider classInfoProvider, final String className, final boolean assumeClassLoader) {
        return is(classInfoProvider, className, "java/security/SecureClassLoader", assumeClassLoader);
    }

    private static boolean isClassLoader(final ClassInfoProvider classInfoProvider, final String className, final boolean assumeClassLoader) {
        return is(classInfoProvider, className, "java/lang/ClassLoader", assumeClassLoader);
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

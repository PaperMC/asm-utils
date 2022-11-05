package io.papermc.reflectionrewriter;

import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class DefineClassRule {
    private static final Set<String> DEFINE_CLASS_DESCS = Set.of(
        "([BII)Ljava/lang/Class;",
        "(Ljava/lang/String;[BII)Ljava/lang/Class;",
        "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;)Ljava/lang/Class;",
        "(Ljava/lang/String;Ljava/nio/ByteBuffer;Ljava/security/ProtectionDomain;)Ljava/lang/Class;"
    );

    private DefineClassRule() {
    }

    /**
     * Create a rewrite rule for MethodHandles.Lookup#defineClass and ClassLoader#defineClass.
     *
     * @param proxyClassName proxy class name
     * @return new rule
     */
    public static RewriteRule create(final String proxyClassName) {
        return create(proxyClassName, false);
    }

    /**
     * Create a rewrite rule for MethodHandles.Lookup#defineClass and ClassLoader#defineClass.
     *
     * @param proxyClassName    proxy class name
     * @param assumeClassLoader whether to assume a class is a {@link ClassLoader} if it cannot be determined
     *                          using {@code classInfoProvider}
     * @return new rule
     */
    public static RewriteRule create(final String proxyClassName, final boolean assumeClassLoader) {
        return RewriteRule.create((InvokeStaticRewrite) (classInfoProvider, owner, name, descriptor, isInterface) -> {
            if (name.equals("defineClass") && DEFINE_CLASS_DESCS.contains(descriptor) && isClassLoader(classInfoProvider, owner, assumeClassLoader)) {
                final String redirectedDescriptor = "(Ljava/lang/ClassLoader;" + descriptor.substring(1);
                return InvokeStaticRewrite.staticRedirect(proxyClassName, name, redirectedDescriptor);
            } else if (owner.equals("java/lang/invoke/MethodHandles$Lookup") && name.equals("defineClass") && descriptor.equals("([B)Ljava/lang/Class;")) {
                final String redirectedDescriptor = "(Ljava/lang/invoke/MethodHandles$Lookup;" + descriptor.substring(1);
                return InvokeStaticRewrite.staticRedirect(proxyClassName, name, redirectedDescriptor);
            }
            return null;
        });
    }

    private static boolean isClassLoader(final ClassInfoProvider classInfoProvider, final String className, final boolean assumeClassLoader) {
        if (className.equals("java/lang/ClassLoader")) {
            return true;
        }
        final @Nullable ClassInfo info = classInfoProvider.info(className);
        if (info != null) {
            if (info.superClass() != null) {
                return isClassLoader(classInfoProvider, info.superClass(), assumeClassLoader);
            } else {
                return false;
            }
        }
        return assumeClassLoader;
    }
}

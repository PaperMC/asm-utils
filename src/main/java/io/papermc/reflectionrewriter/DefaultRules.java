package io.papermc.reflectionrewriter;

import java.util.List;

@SuppressWarnings("checkstyle:UnnecessaryParentheses") // Parens add clarity when reading
public final class DefaultRules {
    private final String proxy;
    private final RewriteRule classRule;
    private final RewriteRule methodHandlesLookupRule;
    private final RewriteRule lambdaMetafactoryRule;
    private final RewriteRule constantBootstrapsRule;
    // TODO Any reflection utils bundled with Paper
    private final RewriteRule methodTypeRule;

    public DefaultRules(final String proxyClassName) {
        this.proxy = proxyClassName;
        this.classRule = this.createClassRule();
        this.methodHandlesLookupRule = this.createMethodHandlesLookupRule();
        this.lambdaMetafactoryRule = this.createLamdaMetafactoryRule();
        this.constantBootstrapsRule = this.createConstantBootstrapsRule();
        this.methodTypeRule = this.createMethodTypeRule();
    }

    public RewriteRule classRule() {
        return this.classRule;
    }

    public RewriteRule methodHandlesLookupRule() {
        return this.methodHandlesLookupRule;
    }

    public RewriteRule lambdaMetafactoryRule() {
        return this.lambdaMetafactoryRule;
    }

    public RewriteRule constantBootstrapsRule() {
        return this.constantBootstrapsRule;
    }

    public RewriteRule methodTypeRule() {
        return this.methodTypeRule;
    }

    public List<RewriteRule> rules() {
        return List.of(
            this.classRule,
            this.methodHandlesLookupRule,
            this.lambdaMetafactoryRule,
            this.constantBootstrapsRule,
            this.methodTypeRule
        );
    }

    private RewriteRule createClassRule() {
        return RewriteRule.create(InvokeStaticRewrite.forOwner(
            "java/lang/Class",
            (classInfoProvider, owner, name, descriptor, isInterface) -> {
                if (((name.equals("getDeclaredField") || name.equals("getField")) && descriptor.equals("(Ljava/lang/String;)Ljava/lang/reflect/Field;"))
                    || ((name.equals("getDeclaredMethod") || name.equals("getMethod")) && descriptor.equals("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"))) {
                    final String redirectedDescriptor = "(Ljava/lang/Class;" + descriptor.substring(1);
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, redirectedDescriptor);
                } else if (name.equals("forName")) {
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, descriptor);
                }
                return null;
            }
        ));
    }

    private RewriteRule createMethodHandlesLookupRule() {
        return RewriteRule.create(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/MethodHandles$Lookup",
            (classInfoProvider, owner, name, descriptor, isInterface) -> {
                if (((name.equals("findStatic") || name.equals("findVirtual")) && descriptor.equals("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"))
                    || (name.equals("findClass") && descriptor.equals("(Ljava/lang/String;)Ljava/lang/Class;"))
                    || (name.equals("findSpecial") && descriptor.equals("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;"))
                    || ((name.equals("findGetter") || name.equals("findSetter") || name.equals("findStaticGetter") || name.equals("findStaticSetter")) && descriptor.equals("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;"))
                    || ((name.equals("findVarHandle") || name.equals("findStaticVarHandle")) && descriptor.equals("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/VarHandle;"))
                    || (name.equals("bind") && descriptor.equals("(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"))) {
                    final String redirectedDescriptor = "(Ljava/lang/invoke/MethodHandles$Lookup;" + descriptor.substring(1);
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, redirectedDescriptor);
                }
                return null;
            }
        ));
    }

    private RewriteRule createLamdaMetafactoryRule() {
        return RewriteRule.create(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/LambdaMetafactory",
            (classInfoProvider, owner, name, descriptor, isInterface) -> {
                if ((name.equals("metafactory") && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"))
                    || (name.equals("altMetafactory") && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"))) {
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, descriptor);
                }
                return null;
            }
        ));
    }

    private RewriteRule createConstantBootstrapsRule() {
        return RewriteRule.create(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/ConstantBootstraps",
            (classInfoProvider, owner, name, descriptor, isInterface) -> {
                if ((name.equals("getStaticFinal") && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/Object;"))
                    || (name.equals("getStaticFinal") && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"))
                    || ((name.equals("fieldVarHandle") || name.equals("staticFieldVarHandle")) && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/invoke/VarHandle;"))) {
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, descriptor);
                }
                return null;
            }
        ));
    }

    private RewriteRule createMethodTypeRule() {
        return RewriteRule.create(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/MethodType",
            (classInfoProvider, owner, name, descriptor, isInterface) -> {
                if (name.equals("fromMethodDescriptorString") && descriptor.equals("(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;")) {
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, descriptor);
                }
                return null;
            }
        ));
    }
}

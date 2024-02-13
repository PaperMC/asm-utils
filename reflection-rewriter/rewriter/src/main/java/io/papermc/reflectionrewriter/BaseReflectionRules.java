package io.papermc.reflectionrewriter;

import io.papermc.asm.rules.RewriteRule;
import java.lang.invoke.ConstantBootstraps;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public final class BaseReflectionRules {
    private final String proxy;
    private final RewriteRule classRule;
    private final RewriteRule methodHandlesLookupRule;
    private final RewriteRule lambdaMetafactoryRule;
    private final RewriteRule constantBootstrapsRule;
    private final RewriteRule methodTypeRule;

    public BaseReflectionRules(final String proxyClassName) {
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
        return RewriteRule.forOwner(Class.class, rf -> {
            rf.plainStaticRewrite(this.proxy, b -> b
                .match("getField", "getDeclaredField").desc("(Ljava/lang/String;)Ljava/lang/reflect/Field;")
                .match("getMethod", "getDeclaredMethod").desc("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")
            );
        });
    }

    private RewriteRule createMethodHandlesLookupRule() {
        return RewriteRule.forOwner(MethodHandles.Lookup.class, rf -> {
            rf.plainStaticRewrite(this.proxy, b -> b
                .match("findStatic", "findVirtual").desc("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
                .match("findClass").desc("(Ljava/lang/String;)Ljava/lang/Class;")
                .match("findSpecial").desc("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;")
                .match("findGetter", "findSetter", "findStaticGetter", "findStaticSetter").desc("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;")
                .match("findVarHandle", "findStaticVarHandle").desc("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/VarHandle;")
                .match("bind").desc("(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
            );
        });
    }

    private RewriteRule createLamdaMetafactoryRule() {
        return RewriteRule.forOwner(LambdaMetafactory.class, rf -> {
            rf.plainStaticRewrite(this.proxy, b -> b
                .match("metafactory").desc("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;")
                .match("altMetafactory").desc("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;")
            );
        });
    }

    private RewriteRule createConstantBootstrapsRule() {
        return RewriteRule.forOwner(ConstantBootstraps.class, rf -> {
            rf.plainStaticRewrite(this.proxy, b -> b
                .match("getStaticFinal").desc(
                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/Object;",
                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"
                )
                .match("fieldVarHandle", "staticFieldVarHandle").desc("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/invoke/VarHandle;")
            );
        });
    }

    private RewriteRule createMethodTypeRule() {
        return RewriteRule.forOwner(MethodType.class, rf -> {
            rf.plainStaticRewrite(this.proxy, b -> b
                .match("fromMethodDescriptorString").desc("(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;")
            );
        });
    }
}

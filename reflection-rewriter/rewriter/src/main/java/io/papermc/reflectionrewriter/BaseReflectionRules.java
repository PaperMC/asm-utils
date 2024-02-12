package io.papermc.reflectionrewriter;

import io.papermc.asm.InvokeStaticRewrite;
import io.papermc.asm.MethodMatcher;
import io.papermc.asm.RewriteRule;
import java.util.List;
import java.util.Set;

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
        final MethodMatcher getNamedMatcher = MethodMatcher.builder()
            .match(Set.of("getField", "getDeclaredField"), "(Ljava/lang/String;)Ljava/lang/reflect/Field;")
            .match(Set.of("getMethod", "getDeclaredMethod"), "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")
            .build();
        return RewriteRule.create(InvokeStaticRewrite.forOwner(
            "java/lang/Class",
            (context, opcode, owner, name, descriptor, isInterface) -> {
                if (name.equals("forName")) {
                    return this.redirectToProxy(name, descriptor);
                } else if (getNamedMatcher.matches(name, descriptor)) {
                    return this.redirectToProxy(name, InvokeStaticRewrite.insertFirstParam("java/lang/Class", descriptor));
                }
                return null;
            }
        ));
    }

    private RewriteRule createMethodHandlesLookupRule() {
        final MethodMatcher matcher = MethodMatcher.builder()
            .match(Set.of("findStatic", "findVirtual"), "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
            .match("findClass", "(Ljava/lang/String;)Ljava/lang/Class;")
            .match("findSpecial", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;")
            .match(Set.of("findGetter", "findSetter", "findStaticGetter", "findStaticSetter"), "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;")
            .match(Set.of("findVarHandle", "findStaticVarHandle"), "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/VarHandle;")
            .match("bind", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
            .build();
        return RewriteRule.create(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/MethodHandles$Lookup",
            (context, opcode, owner, name, descriptor, isInterface) -> {
                if (matcher.matches(name, descriptor)) {
                    return this.redirectToProxy(name, InvokeStaticRewrite.insertFirstParam("java/lang/invoke/MethodHandles$Lookup", descriptor));
                }
                return null;
            }
        ));
    }

    private RewriteRule createLamdaMetafactoryRule() {
        final MethodMatcher matcher = MethodMatcher.builder()
            .match("metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;")
            .match("altMetafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;")
            .build();
        return RewriteRule.create(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/LambdaMetafactory",
            (context, opcode, owner, name, descriptor, isInterface) -> {
                if (matcher.matches(name, descriptor)) {
                    return this.redirectToProxy(name, descriptor);
                }
                return null;
            }
        ));
    }

    private RewriteRule createConstantBootstrapsRule() {
        final MethodMatcher matcher = MethodMatcher.builder()
            .match("getStaticFinal", Set.of(
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/Object;",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"
            ))
            .match(Set.of("fieldVarHandle", "staticFieldVarHandle"), "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/invoke/VarHandle;")
            .build();
        return RewriteRule.create(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/ConstantBootstraps",
            (context, opcode, owner, name, descriptor, isInterface) -> {
                if (matcher.matches(name, descriptor)) {
                    return this.redirectToProxy(name, descriptor);
                }
                return null;
            }
        ));
    }

    private RewriteRule createMethodTypeRule() {
        return RewriteRule.create(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/MethodType",
            (context, opcode, owner, name, descriptor, isInterface) -> {
                if (name.equals("fromMethodDescriptorString") && descriptor.equals("(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;")) {
                    return this.redirectToProxy(name, descriptor);
                }
                return null;
            }
        ));
    }

    private InvokeStaticRewrite.Rewrite redirectToProxy(final String name, final String descriptor) {
        return InvokeStaticRewrite.staticRedirect(this.proxy, name, descriptor);
    }
}

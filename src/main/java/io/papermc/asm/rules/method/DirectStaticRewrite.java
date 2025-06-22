package io.papermc.asm.rules.method;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.method.MethodType;
import io.papermc.asm.rules.generate.GeneratedMethodHolder;
import io.papermc.asm.rules.method.params.DirectParameterRewrite;
import io.papermc.asm.rules.method.rewrite.ConstructorRewrite;
import io.papermc.asm.rules.method.rewrite.MethodRewrite;
import io.papermc.asm.rules.method.rewrite.SimpleRewrite;
import io.papermc.asm.util.DescriptorUtils;
import io.papermc.asm.versioned.ApiVersion;
import io.papermc.asm.versioned.VersionedRuleFactory;
import io.papermc.asm.versioned.matcher.VersionedMatcher;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import static io.papermc.asm.util.DescriptorUtils.desc;
import static io.papermc.asm.util.DescriptorUtils.toOwner;
import static io.papermc.asm.util.OpcodeUtils.staticOp;

/**
 * Rewrites a method by just directly routing it to an identical static method in another class.
 *
 * @param owners              the owners to target
 * @param methodMatcher       the method matcher to use
 * @param staticRedirectOwner the owner to redirect to
 */
public record DirectStaticRewrite(Set<ClassDesc> owners, @Nullable String staticMethodName, MethodMatcher methodMatcher, ClassDesc staticRedirectOwner) implements StaticRewrite, OwnableMethodRewriteRule.Filtered {

    public DirectStaticRewrite(final Set<ClassDesc> owners, final MethodMatcher methodMatcher, final ClassDesc staticRedirectOwner) {
        this(owners, null, methodMatcher, staticRedirectOwner);
    }

    @Override
    public MethodRewrite<GeneratedMethodHolder.MethodCallData> createRewrite(final ClassProcessingContext context, final MethodTypeDesc intermediateDescriptor, final GeneratedMethodHolder.MethodCallData originalCallData) {
        if (this.staticMethodName() == null) {
            return StaticRewrite.super.createRewrite(context, intermediateDescriptor, originalCallData);
        } else {
            return new SimpleRewrite(staticOp(originalCallData.isInvokeDynamic()), this.staticRedirectOwner(context), this.staticMethodName(), this.transformToRedirectDescriptor(intermediateDescriptor), false, originalCallData.isInvokeDynamic());
        }
    }

    @Override
    public MethodRewrite<GeneratedMethodHolder.ConstructorCallData> createConstructorRewrite(final ClassProcessingContext context, final MethodTypeDesc intermediateDescriptor, final GeneratedMethodHolder.ConstructorCallData originalCallData) {
        if (this.staticMethodName() == null) {
            return StaticRewrite.super.createConstructorRewrite(context, intermediateDescriptor, originalCallData);
        } else {
            return new ConstructorRewrite(this.staticRedirectOwner(context), toOwner(originalCallData.owner()), this.staticMethodName(), this.transformToRedirectDescriptor(intermediateDescriptor));
        }
    }

    @Override
    public ClassDesc staticRedirectOwner(final ClassProcessingContext context) {
        return this.staticRedirectOwner;
    }

    public record Versioned(Set<ClassDesc> owners, ClassDesc staticRedirectOwner, @Nullable String staticMethodName, VersionedMatcher<MethodMatcher> versions) implements VersionedRuleFactory {

        @Override
        public RewriteRule createRule(final ApiVersion<?> apiVersion) {
            return this.versions.ruleForVersion(apiVersion, match -> new DirectStaticRewrite(this.owners(), this.staticMethodName(), match, this.staticRedirectOwner()));
        }
    }

    /**
     * Annotate a {@code public} {@code static} method with this annotation and then scan it with TODO
     * to automatically generate a {@link DirectParameterRewrite} rule.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Wrapper {

        String IMPLIED_METHOD_NAME = "__%%implied_method_name%%__";

        /**
         * The owners of the method to rewrite.
         *
         * @return the owners
         */
        String[] owners() default {};

        /**
         * The owners of the method to rewrite.
         *
         * @return the owners
         */
        Class<?>[] ownerClasses() default {};

        /**
         * The type of the method to rewrite.
         *
         * @return the type
         */
        MethodType type() default MethodType.VIRTUAL;

        /**
         * The type to convert the parameters to.
         *
         * @return the type to convert to
         */
        String methodName() default IMPLIED_METHOD_NAME;

    }

    @ApiStatus.Internal
    public static DirectStaticRewrite create(final Method method, final Wrapper annotation) {
        final ClassDesc[] owners = new ClassDesc[annotation.owners().length + annotation.ownerClasses().length];
        if (owners.length < 1) {
            throw new IllegalArgumentException("Method " + method.getName() + " must have at least one owner");
        }
        for (int i = 0; i < annotation.owners().length; i++) {
            owners[i] = ClassDesc.of(annotation.owners()[i]);
        }
        for (int i = 0; i < annotation.ownerClasses().length; i++) {
            owners[i + annotation.owners().length] = annotation.ownerClasses()[i].describeConstable().orElseThrow();
        }
        final String targetMethodName = annotation.methodName().equals(Wrapper.IMPLIED_METHOD_NAME) ? method.getName() : annotation.methodName();
        final Class<?>[] parameterTypes;
        if (annotation.type() == MethodType.VIRTUAL || annotation.type() == MethodType.INTERFACE) {
            if (method.getParameterTypes().length < 1) {
                throw new IllegalArgumentException("Method " + targetMethodName + " requires at least one parameter");
            }
            parameterTypes = new Class<?>[method.getParameterTypes().length - 1];
            System.arraycopy(method.getParameterTypes(), 1, parameterTypes, 0, parameterTypes.length);
        } else {
            parameterTypes = method.getParameterTypes();
        }
        final ClassDesc[] parameterDescs = Arrays.stream(parameterTypes).map(DescriptorUtils::desc).toArray(ClassDesc[]::new);
        final MethodTypeDesc methodDesc = MethodTypeDesc.of(desc(method.getReturnType()), parameterDescs);
        final MethodMatcher matcher = MethodMatcher.builder()
            .match(targetMethodName, b -> b.type(annotation.type()).desc(methodDesc))
            .build();
        return new DirectStaticRewrite(
            Set.of(owners),
            method.getName(),
            matcher,
            desc(method.getDeclaringClass())
        );
    }
}

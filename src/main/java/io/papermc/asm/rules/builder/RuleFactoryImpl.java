package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.FieldMatcher;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import io.papermc.asm.rules.field.FieldRewrites;
import io.papermc.asm.rules.method.MethodRewrites;
import io.papermc.asm.rules.method.StaticRewrite;
import io.papermc.asm.rules.method.StaticRewrites;
import io.papermc.asm.util.Builder;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.papermc.asm.util.DescriptorUtils.desc;

class RuleFactoryImpl implements RuleFactory {

    final Set<Class<?>> owners;
    final List<RewriteRule> rules = new ArrayList<>();

    RuleFactoryImpl(final Set<Class<?>> owners) {
        this.owners = Set.copyOf(owners);
    }

    private static <M, B extends Builder<M>> M build(final Consumer<? super B> builderConsumer, final Supplier<B> supplier) {
        final B builder = supplier.get();
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Override
    public void plainStaticRewrite(final ClassDesc newOwner, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        this.addRule(new StaticRewrite.Plain(this.owners, build(builderConsumer, MethodMatcher::builder), newOwner));
    }

    @Override
    public void changeParamToSuper(final Class<?> oldParamType, final Class<?> newParamType, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        if (!newParamType.isAssignableFrom(oldParamType)) {
            throw new IllegalArgumentException(newParamType + " is not a superclass of " + oldParamType);
        }
        this.addRule(new MethodRewrites.SuperTypeParam(this.owners, build(builderConsumer, MethodMatcher::builder), desc(oldParamType), desc(newParamType)));
    }

    @Override
    public void changeParamFuzzy(final Supplier<Class<?>> newOwner, final Class<?> newType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.addRule(new StaticRewrites.FuzzyParam(this.owners, desc(newType), build(builderConsumer, MethodMatcher::targeted), convert(newOwner), verify(staticHandler)));
    }

    @Override
    public void changeParamDirect(final Supplier<Class<?>> newOwner, final Class<?> existingParam, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.addRule(new StaticRewrites.DirectParam(this.owners, desc(existingParam), build(builderConsumer, MethodMatcher::targeted), convert(newOwner), verify(staticHandler)));
    }

    @Override
    public void changeReturnTypeToSub(final Class<?> oldReturnType, final Class<?> newReturnType, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        this.addRule(new MethodRewrites.SubTypeReturn(this.owners, build(builderConsumer, MethodMatcher::builder), desc(oldReturnType), desc(newReturnType)));
    }

    @Override
    public void changeReturnTypeFuzzy(final Supplier<Class<?>> newOwner, final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.addRule(StaticRewrites.returnRewrite(this.owners, desc(newReturnType), build(builderConsumer, MethodMatcher::targeted), convert(newOwner), verify(staticHandler), StaticRewrites.OBJECT_DESC, false));
    }

    @Override
    public void changeReturnTypeDirect(final Supplier<Class<?>> newOwner, final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.addRule(StaticRewrites.returnRewrite(this.owners, desc(newReturnType), build(builderConsumer, MethodMatcher::targeted), convert(newOwner), verify(staticHandler), desc(newReturnType), false));
    }

    @Override
    public void changeReturnTypeFuzzyWithContext(final Supplier<Class<?>> newOwner, final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.addRule(StaticRewrites.returnRewrite(this.owners, desc(newReturnType), build(builderConsumer, MethodMatcher::targeted), convert(newOwner), verify(staticHandler), StaticRewrites.OBJECT_DESC, true));
    }

    @Override
    public void changeReturnTypeDirectWithContext(final Supplier<Class<?>> newOwner, final Class<?> newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.addRule(StaticRewrites.returnRewrite(this.owners, desc(newReturnType), build(builderConsumer, MethodMatcher::targeted), convert(newOwner), verify(staticHandler), desc(newReturnType), true));
    }

    @Override
    public void renameField(final String newName, final Consumer<? super FieldMatcher.Builder> builderConsumer) {
        this.addRule(new FieldRewrites.Rename(this.owners, build(builderConsumer, FieldMatcher::builder), newName));
    }

    @Override
    public void addRule(final RewriteRule rule) {
        this.rules.add(rule);
    }

    private static Method verify(final Method staticHandler) {
        if (!Modifier.isStatic(staticHandler.getModifiers())) {
            throw new IllegalArgumentException(staticHandler + " isn't a static method");
        }
        return staticHandler;
    }

    private static Supplier<ClassDesc> convert(final Supplier<Class<?>> classSupplier) {
        return () -> classSupplier.get().describeConstable().orElseThrow();
    }

    @Override
    public RewriteRule build() {
        if (this.rules.size() == 1) {
            return this.rules.get(0);
        }
        return RewriteRule.chain(this.rules);
    }
}

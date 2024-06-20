package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.FieldMatcher;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import io.papermc.asm.rules.field.FieldRewrites;
import io.papermc.asm.rules.method.MethodRewrites;
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
import org.checkerframework.checker.nullness.qual.Nullable;

class RuleFactoryImpl implements RuleFactory {

    final Set<ClassDesc> owners;
    final List<RewriteRule> rules = new ArrayList<>();

    RuleFactoryImpl(final Set<ClassDesc> owners) {
        this.owners = Set.copyOf(owners);
    }

    private static <M, B extends Builder<M>> M build(final Consumer<? super B> builderConsumer, final Supplier<B> supplier) {
        final B builder = supplier.get();
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Override
    public void plainStaticRewrite(final ClassDesc newOwner, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        this.addRule(new StaticRewrites.Plain(this.owners, build(builderConsumer, MethodMatcher::builder), newOwner));
    }

    @Override
    public void changeParamToSuper(final ClassDesc legacyParamType, final ClassDesc newParamType, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        this.addRule(new MethodRewrites.SuperTypeParam(this.owners, build(builderConsumer, MethodMatcher::builder), legacyParamType, newParamType));
    }

    @Override
    public void changeParamFuzzy(final ClassDesc modernParamType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.addRule(new StaticRewrites.FuzzyParam(this.owners, modernParamType, build(builderConsumer, MethodMatcher::targeted), isStatic(staticHandler)));
    }

    @Override
    public void changeParamDirect(final ClassDesc existingParam, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.addRule(new StaticRewrites.DirectParam(this.owners, existingParam, build(builderConsumer, MethodMatcher::targeted), isStatic(staticHandler)));
    }

    @Override
    public void changeReturnTypeToSub(final ClassDesc oldReturnType, final ClassDesc newReturnType, final Consumer<? super MethodMatcher.Builder> builderConsumer) {
        this.addRule(new MethodRewrites.SubTypeReturn(this.owners, build(builderConsumer, MethodMatcher::builder), oldReturnType, newReturnType));
    }

    @Override
    public void changeReturnTypeDirect(final ClassDesc newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirect(newReturnType, staticHandler, builderConsumer, false);
    }

    @Override
    public void changeReturnTypeDirectWithContext(final ClassDesc newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer) {
        this.changeReturnTypeDirect(newReturnType, staticHandler, builderConsumer, true);
    }

    private void changeReturnTypeDirect(final ClassDesc newReturnType, final Method staticHandler, final Consumer<? super TargetedMethodMatcher.Builder> builderConsumer, final boolean includeOwnerContext) {
        final TargetedMethodMatcher matcher = build(builderConsumer, MethodMatcher::targeted);
        this.addRule(new StaticRewrites.DirectReturn(this.owners, newReturnType, matcher, isStatic(staticHandler), includeOwnerContext));
    }

    @Override
    public void changeFieldToMethod(final Consumer<? super FieldMatcher.Builder> builderConsumer, final @Nullable String getterName, final @Nullable String setterName, final boolean isInterfaceMethod) {
        this.addRule(new FieldRewrites.ToMethodSameOwner(this.owners, build(builderConsumer, FieldMatcher::builder), getterName, setterName, isInterfaceMethod));
    }

    @Override
    public void addRule(final RewriteRule rule) {
        this.rules.add(rule);
    }

    private static Method isStatic(final Method staticHandler) {
        if (!Modifier.isStatic(staticHandler.getModifiers())) {
            throw new IllegalArgumentException(staticHandler + " isn't a static method");
        }
        return staticHandler;
    }

    @Override
    public RewriteRule build() {
        if (this.rules.size() == 1) {
            return this.rules.get(0);
        }
        return RewriteRule.chain(this.rules);
    }
}

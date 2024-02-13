package io.papermc.asm;

import io.papermc.asm.rules.RewriteRule;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.ClassVisitor;

@DefaultQualifier(NonNull.class)
public final class RewriteRuleVisitorFactory {

    private final int api;
    private final Supplier<RewriteRule> ruleFactory;
    private volatile @MonotonicNonNull RewriteRule rule;
    private final ClassInfoProvider classInfoProvider;

    public RewriteRuleVisitorFactory(
        final int api,
        final Supplier<RewriteRule> ruleFactory,
        final ClassInfoProvider classInfoProvider
    ) {
        this.api = api;
        this.ruleFactory = ruleFactory;
        this.classInfoProvider = classInfoProvider;
    }

    public RewriteRuleVisitorFactory(
        final int api,
        final RewriteRule rule,
        final ClassInfoProvider classInfoProvider
    ) {
        this(api, () -> rule, classInfoProvider);
    }

    public ClassVisitor createVisitor(final ClassVisitor parent) {
        final MutableProcessingContext context = new MutableProcessingContext();
        final ClassVisitor ruleVisitor = this.rule().createVisitor(this.api, parent, context);
        return new ContextFillerVisitor(this.api, ruleVisitor, context);
    }

    private RewriteRule rule() {
        @Nullable RewriteRule rule = this.rule;
        if (rule != null) {
            return rule;
        }

        synchronized (this) {
            rule = this.rule;
            if (rule == null) {
                rule = this.ruleFactory.get();
                this.rule = rule;
            }
        }

        return rule;
    }

    private final class MutableProcessingContext implements ClassProcessingContext {
        private @MonotonicNonNull String name;
        private @Nullable String superName;

        @Override
        public ClassInfoProvider classInfoProvider() {
            return RewriteRuleVisitorFactory.this.classInfoProvider;
        }

        @Override
        public String processingClassName() {
            return this.name;
        }

        @Override
        public @Nullable String processingClassSuperClassName() {
            return this.superName;
        }
    }

    private static final class ContextFillerVisitor extends ClassVisitor {

        private final MutableProcessingContext context;

        private ContextFillerVisitor(final int api, final ClassVisitor classVisitor, final MutableProcessingContext context) {
            super(api, classVisitor);
            this.context = context;
        }

        @Override
        public void visit(final int version, final int access, final String name, final @Nullable String signature, final @Nullable String superName, final String @Nullable [] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.context.name = name;
            this.context.superName = superName;
        }
    }
}

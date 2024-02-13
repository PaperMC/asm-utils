package io.papermc.asm;

import io.papermc.asm.rules.RewriteRule;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.ClassVisitor;

@DefaultQualifier(NonNull.class)
public class RewriteRulesVisitorFactory implements ClassProcessingContext {

    private @MonotonicNonNull RewriteRule rootRule;
    private final int api;
    private final ClassVisitor parent;
    private final ClassInfoProvider classInfoProvider;

    private @MonotonicNonNull String name;
    private @Nullable String superName;

    public RewriteRulesVisitorFactory(final int api, final ClassVisitor parent, final ClassInfoProvider classInfoProvider) {
        this.api = api;
        this.parent = parent;
        this.classInfoProvider = classInfoProvider;
    }

    public ClassVisitor createVisitor() {
        final ClassVisitor ruleVisitor = this.rootRule().createVisitor(this.api, this.parent, this);
        return new ContextFillerVisitor(this.api, ruleVisitor);
    }

    protected RewriteRule createRootRule() {
        return RewriteRule.EMPTY;
    }

    private RewriteRule rootRule() {
        if (this.rootRule == null) {
            try {
                this.rootRule = this.createRootRule();
            } catch (final Throwable throwable) {
                this.rootRule = RewriteRule.EMPTY;
                throw throwable;
            }
        }
        return this.rootRule;
    }

    @Override
    public final ClassInfoProvider classInfoProvider() {
        return this.classInfoProvider;
    }

    @Override
    public final String processingClassName() {
        return this.name;
    }

    @Override
    public final @Nullable String processingClassSuperClassName() {
        return this.superName;
    }

    private final class ContextFillerVisitor extends ClassVisitor {

        private ContextFillerVisitor(final int api, final ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public void visit(final int version, final int access, final String name, final @Nullable String signature, final @Nullable String superName, final String @Nullable [] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            RewriteRulesVisitorFactory.this.name = name;
            RewriteRulesVisitorFactory.this.superName = superName;
        }
    }
}

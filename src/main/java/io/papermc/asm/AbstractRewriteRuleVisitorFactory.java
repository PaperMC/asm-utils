package io.papermc.asm;

import io.papermc.asm.rules.RewriteRule;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;

public abstract class AbstractRewriteRuleVisitorFactory implements RewriteRuleVisitorFactory {

    private final int api;
    private volatile @Nullable RewriteRule rule;
    private final ClassInfoProvider classInfoProvider;

    protected AbstractRewriteRuleVisitorFactory(
        final int api,
        final ClassInfoProvider classInfoProvider
    ) {
        this.api = api;
        this.classInfoProvider = classInfoProvider;
    }

    protected abstract RewriteRule createRule();

    @Override
    public ClassVisitor createVisitor(final ClassVisitor parent) {
        final MutableProcessingContext context = new MutableProcessingContext();
        final ClassVisitor ruleVisitor = this.rule().createVisitor(this.api, parent, context);
        // context filler should be last, so it is run before all the wrapped visitors
        return new ContextFillerVisitor(this.api, ruleVisitor, context);
    }

    protected final RewriteRule rule() {
        RewriteRule rule = this.rule;
        if (rule != null) {
            return rule;
        }

        synchronized (this) {
            rule = this.rule;
            if (rule == null) {
                rule = this.createRule();
                this.rule = rule;
            }
        }

        return rule;
    }

    private final class MutableProcessingContext implements ClassProcessingContext {
        private static final String NULL = "null";

        private @Nullable String name = null;
        private @Nullable String superName = NULL;

        @Override
        public ClassInfoProvider classInfoProvider() {
            return AbstractRewriteRuleVisitorFactory.this.classInfoProvider;
        }

        @Override
        public String processingClassName() {
            if (this.name == null) {
                throw new IllegalStateException("processingClassName is only available after the class header is visited.");
            }
            return this.name;
        }

        @SuppressWarnings("StringEquality")
        @Override
        public @Nullable String processingClassSuperClassName() {
            if (this.superName == NULL) {
                throw new IllegalStateException("processingClassSuperClassName is only available after the class header is visited.");
            }
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

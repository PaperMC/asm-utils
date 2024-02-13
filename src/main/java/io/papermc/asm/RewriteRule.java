package io.papermc.asm;

import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.MethodVisitor;

@DefaultQualifier(NonNull.class)
public interface RewriteRule extends ShouldProcess, MethodVisitorFactory {
    static RewriteRule create(final MethodVisitorFactory methodVisitorFactory) {
        return new BasicRewriteRule($ -> true, methodVisitorFactory);
    }

    static RewriteRule create(final ShouldProcess shouldProcess, final MethodVisitorFactory methodVisitorFactory) {
        return new BasicRewriteRule(shouldProcess, methodVisitorFactory);
    }

    static Chain chain(final RewriteRule... rules) {
        return chain(Arrays.asList(rules));
    }

    static Chain chain(final List<RewriteRule> rules) {
        return new Chain(rules);
    }

    record Chain(List<RewriteRule> rules) implements RewriteRule {
        public Chain(final List<RewriteRule> rules) {
            this.rules = List.copyOf(rules);
        }

        @Override
        public boolean shouldProcess(final ClassProcessingContext context) {
            for (final RewriteRule rule : this.rules) {
                if (rule.shouldProcess(context)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public MethodVisitor createVisitor(final int api, final MethodVisitor parent, final ClassProcessingContext context) {
            MethodVisitor visitor = parent;
            for (final RewriteRule rule : this.rules) {
                if (rule.shouldProcess(context)) {
                    visitor = rule.createVisitor(api, visitor, context);
                }
            }
            return visitor;
        }
    }
}

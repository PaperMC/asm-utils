package io.papermc.reflectionrewriter;

import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.MethodVisitor;

@DefaultQualifier(NonNull.class)
public interface RewriteRule extends MethodVisitorFactory, ShouldProcess {
    static RewriteRule create(final MethodVisitorFactory methodVisitorFactory) {
        return new BasicRewriteRule($ -> true, methodVisitorFactory);
    }

    static RewriteRule create(final ShouldProcess shouldProcess, final MethodVisitorFactory methodVisitorFactory) {
        return new BasicRewriteRule(shouldProcess, methodVisitorFactory);
    }

    static RewriteRule chain(final RewriteRule... rules) {
        return chain(Arrays.asList(rules));
    }

    static RewriteRule chain(final List<RewriteRule> rules) {
        final List<RewriteRule> rulesCopy = List.copyOf(rules);
        return RewriteRule.create(
            context -> rulesCopy.stream().anyMatch(rule -> rule.shouldProcess(context)),
            (api, parent, context) -> {
                MethodVisitor visitor = parent;
                for (final RewriteRule rule : rulesCopy) {
                    if (rule.shouldProcess(context)) {
                        visitor = rule.createVisitor(api, visitor, context);
                    }
                }
                return visitor;
            }
        );
    }
}

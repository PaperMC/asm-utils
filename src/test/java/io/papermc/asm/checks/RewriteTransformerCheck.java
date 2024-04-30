package io.papermc.asm.checks;

import io.papermc.asm.TestUtil;
import io.papermc.asm.rules.RewriteRule;

public record RewriteTransformerCheck(String className) implements TransformerCheck {

    @Override
    public void run(final RewriteRule rule) {
        TestUtil.assertProcessedMatchesExpected(this.className, TestUtil.testingVisitorFactory(rule));
    }
}

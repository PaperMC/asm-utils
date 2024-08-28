package io.papermc.asm.checks;

import io.papermc.asm.TestUtil;
import io.papermc.asm.rules.RewriteRule;

public record ExecutionTransformerCheck(String className, boolean copyFromClassReader) implements TransformerCheck {

    @Override
    public void run(final RewriteRule rule) {
        TestUtil.processAndExecute(this.className, new TestUtil.DefaultProcessor(TestUtil.testingVisitorFactory(rule), this.copyFromClassReader()));
    }
}

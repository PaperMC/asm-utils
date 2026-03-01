package io.papermc.classfile.checks;

import io.papermc.classfile.RewriteProcessor;
import io.papermc.classfile.TestUtil;

public record RewriteTransformerCheck(String className) implements TransformerCheck {

    @Override
    public void run(final RewriteProcessor rewriteProcessor) {
        TestUtil.assertProcessedMatchesExpected(this.className, rewriteProcessor);
    }
}

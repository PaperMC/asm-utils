package io.papermc.classfile.checks;

import io.papermc.classfile.RewriteProcessor;
import io.papermc.classfile.TestUtil;

public record ExecutionTransformerCheck(String className) implements TransformerCheck {

    @Override
    public void run(final RewriteProcessor rewrite) {
        TestUtil.processAndExecute(this.className, new TestUtil.DefaultProcessor(rewrite));
    }
}

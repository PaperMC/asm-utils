package io.papermc.classfile.checks;

import io.papermc.classfile.RewriteProcessor;

public interface TransformerCheck {

    void run(RewriteProcessor rule);
}

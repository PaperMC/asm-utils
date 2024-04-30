package io.papermc.asm.checks;

import io.papermc.asm.rules.RewriteRule;

public interface TransformerCheck {

    void run(RewriteRule rule);
}

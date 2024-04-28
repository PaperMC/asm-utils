package io.papermc.asm;

import data.SameClassTarget;
import io.papermc.asm.rules.RewriteRule;
import org.junit.jupiter.api.Test;

import static io.papermc.asm.TestUtil.testingVisitorFactory;

class TestSameClassTarget {
    @Test
    void simpleFieldRename() {
        final RewriteRule renameRule = RewriteRule.forOwner(SameClassTarget.class, builder -> {
            builder.renameField("B", fieldBuilder -> fieldBuilder.names("A"));
        });
        final RewriteRuleVisitorFactory visitorFactory = testingVisitorFactory(renameRule);

        TestUtil.assertProcessedMatchesExpected("data/SameClassTargetUser", visitorFactory);
    }
}

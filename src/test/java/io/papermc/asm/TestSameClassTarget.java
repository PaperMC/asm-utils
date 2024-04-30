package io.papermc.asm;

import data.SameClassTarget;
import io.papermc.asm.rules.RewriteRule;
import org.junit.jupiter.api.Test;

import static io.papermc.asm.TestUtil.testingVisitorFactory;

class TestSameClassTarget {
    @Test
    void simpleFieldRename() {
        final RewriteRuleVisitorFactory visitorFactory = simpleRenameFactory();

        TestUtil.assertProcessedMatchesExpected("data/SameClassTargetUser", visitorFactory);
    }

    @Test
    void simpleFieldRenameExecute() {
        final RewriteRuleVisitorFactory visitorFactory = simpleRenameFactory();

        TestUtil.processAndExecute(
            "data/SameClassTargetUser",
            new TestUtil.DefaultProcessor(visitorFactory)
        );
    }

    private static RewriteRuleVisitorFactory simpleRenameFactory() {
        final RewriteRule renameRule = RewriteRule.forOwner(SameClassTarget.class, builder -> {
            builder.renameField("B", fieldBuilder -> fieldBuilder.names("A"));
        });
        return testingVisitorFactory(renameRule);
    }
}

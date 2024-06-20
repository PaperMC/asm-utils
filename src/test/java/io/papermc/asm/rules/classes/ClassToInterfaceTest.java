package io.papermc.asm.rules.classes;

import data.types.classes.AbstractSomeAbstractClass;
import data.types.classes.SomeAbstractClass;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;

class ClassToInterfaceTest {

    @TransformerTest("data/classes/ClassToInterfaceUser")
    void testWithNoReplacement(final TransformerCheck check) {
        final RewriteRule rule = new ClassToInterfaceRule(
            SomeAbstractClass.class.describeConstable().orElseThrow(),
            null
        );

        check.run(rule);
    }

    @TransformerTest("data/classes/ClassToInterfaceRedirectUser")
    void testWithReplacement(final TransformerCheck check) {
        final RewriteRule rule = new ClassToInterfaceRule(
            SomeAbstractClass.class.describeConstable().orElseThrow(),
            AbstractSomeAbstractClass.class.describeConstable().orElseThrow()
        );

        check.run(rule);
    }
}

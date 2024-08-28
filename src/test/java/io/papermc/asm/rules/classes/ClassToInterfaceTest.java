package io.papermc.asm.rules.classes;

import data.types.classes.AbstractSomeAbstractClass;
import data.types.classes.SomeAbstractClass;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;

class ClassToInterfaceTest {

    @TransformerTest(value = "data.classes.ClassToInterfaceUser", copyFromClassReader = false /*required until asm merges https://gitlab.ow2.org/asm/asm/-/merge_requests/403*/)
    void testWithNoReplacement(final TransformerCheck check) {
        final RewriteRule rule = new ClassToInterfaceRule(
            SomeAbstractClass.class.describeConstable().orElseThrow(),
            null
        );

        check.run(rule);
    }

    @TransformerTest(value = "data.classes.ClassToInterfaceRedirectUser", copyFromClassReader = false /*required until asm merges https://gitlab.ow2.org/asm/asm/-/merge_requests/403*/)
    void testWithReplacement(final TransformerCheck check) {
        final RewriteRule rule = new ClassToInterfaceRule(
            SomeAbstractClass.class.describeConstable().orElseThrow(),
            AbstractSomeAbstractClass.class.describeConstable().orElseThrow()
        );

        check.run(rule);
    }
}

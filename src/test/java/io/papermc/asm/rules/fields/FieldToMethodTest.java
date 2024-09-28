package io.papermc.asm.rules.fields;

import data.types.fields.FieldHolder;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.ClassDesc;

import static io.papermc.asm.util.DescriptorUtils.desc;

class FieldToMethodTest {

    static final ClassDesc STRING = desc(String.class);

    @TransformerTest("data.fields.FieldToMethodSameOwnerUser")
    void testFieldToMethodSameOwner(final TransformerCheck check) {
        final RewriteRule rule = RewriteRule.forOwnerClass(FieldHolder.class, builder -> {
            builder.changeFieldToMethod(
                "getStaticField", "setStaticField",
                false,
                b -> b.match("staticField", STRING)
            );
            builder.changeFieldToMethod(
                "getInstanceField", "setInstanceField",
                false,
                b -> b.match("instanceField", STRING)
            );
        });

        check.run(rule);
    }
}

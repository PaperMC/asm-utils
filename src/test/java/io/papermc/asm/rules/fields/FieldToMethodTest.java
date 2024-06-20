package io.papermc.asm.rules.fields;

import data.types.fields.FieldHolder;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.FieldMatcher;
import io.papermc.asm.rules.field.FieldRewrites;
import java.lang.constant.ClassDesc;
import java.util.Set;

import static io.papermc.asm.util.DescriptorUtils.desc;

class FieldToMethodTest {

    static final ClassDesc STRING = desc(String.class);

    @TransformerTest("data/fields/FieldToMethodSameOwnerUser")
    void testFieldToMethodSameOwner(final TransformerCheck check) {
        final RewriteRule staticRule = new FieldRewrites.ToMethodSameOwner(Set.of(FieldHolder.class),
            FieldMatcher.builder()
                .names("staticField")
                .desc(STRING)
                .build(),
            "getStaticField",
            "setStaticField",
            false
        );
        final RewriteRule instanceRule = new FieldRewrites.ToMethodSameOwner(Set.of(FieldHolder.class),
            FieldMatcher.builder()
                .names("instanceField")
                .desc(STRING)
                .build(),
            "getInstanceField",
            "setInstanceField",
            false
        );
        check.run(RewriteRule.chain(staticRule, instanceRule));
    }
}

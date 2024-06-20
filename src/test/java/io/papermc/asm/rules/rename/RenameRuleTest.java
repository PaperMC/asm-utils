package io.papermc.asm.rules.rename;

import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;

class RenameRuleTest {

    @TransformerTest("data.rename.RenameTest")
    void testAnnotationSpecificRenames(final TransformerCheck check) {
        final RenameRule rule = RenameRule.builder()
            .type("data/types/rename/TestEnum", "data/types/rename/RenamedTestEnum")
            .fieldByInternal("data/types/rename/TestEnum", "A", "ONE")
            .fieldByInternal("data/types/rename/TestEnum", "B", "TWO")
            .fieldByInternal("data/types/rename/TestEnum", "C", "THREE")
            .annotationAttribute("Ldata/types/rename/TestAnnotation;", "single", "value")
            .methodByInternal("data/types/rename/TestAnnotation", "single", "()Ldata/types/rename/TestEnum;", "value")
            .build();

        check.run(rule);
    }
}

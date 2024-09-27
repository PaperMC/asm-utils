package io.papermc.asm.rules.rename;

import data.types.rename.TestEnum;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;

class RenameRuleTest {

    @TransformerTest("data.rename.RenameTest")
    void testAnnotationSpecificRenames(final TransformerCheck check) {
        final RenameRule rule = RenameRule.builder()
            .type("data/types/rename/TestEnum", "data/types/rename/RenamedTestEnum")
            .editEnum(TestEnum.class, builder -> {
                builder
                    .rename("A", "ONE")
                    .rename("B", "TWO")
                    .rename("C", "THREE")
                    .rename("FB", "FOUR")
                    .rename("Ea", "FIVE");
            })
            .annotationAttribute("Ldata/types/rename/TestAnnotation;", "single", "value")
            .methodByInternal("data/types/rename/TestAnnotation", "single", "()Ldata/types/rename/TestEnum;", "value")
            .build();

        check.run(rule);
    }
}

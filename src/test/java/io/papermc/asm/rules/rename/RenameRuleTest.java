package io.papermc.asm.rules.rename;

import data.types.rename.RenamedTestEnum;
import data.types.rename.TestAnnotation;
import io.papermc.asm.ApiVersion;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.versioned.VersionedRuleFactory;
import java.lang.constant.ClassDesc;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static io.papermc.asm.util.DescriptorUtils.fromOwner;
import static io.papermc.asm.util.DescriptorUtils.methodDesc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RenameRuleTest {

    private static final ClassDesc TEST_ENUM = fromOwner("data/types/rename/TestEnum");

    @TransformerTest("data.rename.RenameTest")
    void testRenamerRule(final TransformerCheck check) {
        final RenameRule rule = RenameRule.builder()
            .type("data/types/rename/TestEnum", RenamedTestEnum.class)
            .editEnum(TEST_ENUM, builder -> {
                builder
                    .rename("A", "ONE")
                    .rename("B", "TWO")
                    .rename("C", "THREE")
                    .rename("FB", "FOUR")
                    .rename("Ea", "FIVE");
            })
            .annotationAttribute(TestAnnotation.class, "single", "value")
            .methodByClass(TestAnnotation.class, "single", methodDesc("()Ldata/types/rename/TestEnum;"), "value")
            .build();

        check.run(rule);
    }

    @Test
    void testVersionedRenamerRule() {
        final Map<ApiVersion, RenameRule> versions = new HashMap<>();
        versions.put(ApiVersion.ONE, RenameRule.builder()
            .methodByClass(TestAnnotation.class, "single", methodDesc("()Ldata/types/rename/TestEnum;"), "value")
            .editEnum(TEST_ENUM, builder -> builder
                .rename("A", "ONE")
            )
            .build()
        );
        versions.put(ApiVersion.THREE, RenameRule.builder()
            .methodByClass(TestAnnotation.class, "newValue", methodDesc("()Ldata/types/rename/TestEnum;"), "value")
            .annotationAttribute(TestAnnotation.class, "newValue", "value")
            .editEnum(TEST_ENUM, builder -> builder
                .rename("OTHER_A", "ONE")
                .rename("B", "TWO")
            )
            .build()
        );

        final VersionedRuleFactory factory = new RenameRule.Versioned(new TreeMap<>(versions));
        final RenameRule ruleOne = (RenameRule) factory.createRule(ApiVersion.ONE);
        final RenameRule ruleTwo = (RenameRule) factory.createRule(ApiVersion.TWO);
        assertEquals(method("single").apply(ruleOne), "value");
        assertEquals(method("newValue").apply(ruleOne), "value");

        assertNull(method("single").apply(ruleTwo));
        assertEquals(method("newValue").apply(ruleTwo), "value");

    }

    private static Function<RenameRule, @Nullable String> method(final String legacyName) {
        return renameRule -> renameRule.renames().get("%s.%s%s".formatted("data/types/rename/TestAnnotation", legacyName, "()Ldata/types/rename/TestEnum;"));
    }
}

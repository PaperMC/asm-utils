package io.papermc.asm.rules.rename;

import data.types.rename.RenamedTestEnum;
import data.types.rename.TestAnnotation;
import io.papermc.asm.ApiVersions;
import io.papermc.asm.TestApiVersionImpl;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.versioned.MergingVersionRuleFactory;
import io.papermc.asm.versioned.VersionedRuleFactory;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import static io.papermc.asm.util.DescriptorUtils.fromOwner;
import static io.papermc.asm.util.DescriptorUtils.methodDesc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RenameRuleTest {

    private static final ClassDesc TEST_ENUM = fromOwner("data/types/rename/TestEnum");

    @TransformerTest("data.rename.RenameTest")
    void testRenamerRule(final TransformerCheck check) {
        final RenameRule rule = RenameRule.builder(Opcodes.ASM9)
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
            .methodPredicate(TEST_ENUM, MethodMatcher.create(Set.of("method1", "method2"), b -> b.returnType().equals(ConstantDescs.CD_void) && b.parameterList().contains(ConstantDescs.CD_int)), s -> "renamed_" + s)
            .build();

        check.run(rule);
    }

    @Test
    void testVersionedRenamerRule() {
        final Map<TestApiVersionImpl, RenameRule> versions = new HashMap<>();
        versions.put(ApiVersions.ONE, RenameRule.builder(Opcodes.ASM9)
            .methodByClass(TestAnnotation.class, "single", methodDesc("()Ldata/types/rename/TestEnum;"), "value")
            .editEnum(TEST_ENUM, builder -> builder
                .rename("A", "ONE")
            )
            .build()
        );
        versions.put(ApiVersions.THREE, RenameRule.builder(Opcodes.ASM9)
            .methodByClass(TestAnnotation.class, "newValue", methodDesc("()Ldata/types/rename/TestEnum;"), "value")
            .annotationAttribute(TestAnnotation.class, "newValue", "value")
            .editEnum(TEST_ENUM, builder -> builder
                .rename("OTHER_A", "ONE")
                .rename("B", "TWO")
            )
            .build()
        );

        final VersionedRuleFactory factory = MergingVersionRuleFactory.mergeable(new TreeMap<>(versions));
        final RenameRule ruleOne = (RenameRule) factory.createRule(ApiVersions.ONE);
        final RenameRule ruleTwo = (RenameRule) factory.createRule(ApiVersions.TWO);
        assertEquals("value", annotationMethod("single").apply(ruleOne));
        assertEquals("value", annotationMethod("newValue").apply(ruleOne));

        assertNull(annotationMethod("single").apply(ruleTwo));
        assertEquals("value", annotationMethod("newValue").apply(ruleTwo));

        assertEquals("ONE", enumField("A").apply(ruleOne));
        assertEquals("ONE", enumField("OTHER_A").apply(ruleOne));
        assertEquals("TWO", enumField("B").apply(ruleOne));

        assertEquals("ONE", enumField("OTHER_A").apply(ruleTwo));
        assertNull(enumField("A").apply(ruleTwo));
        assertEquals("TWO", enumField("B").apply(ruleTwo));
    }

    private static Function<RenameRule, @Nullable String> annotationMethod(final String legacyName) {
        return renameRule -> renameRule.renames().get("%s.%s%s".formatted("data/types/rename/TestAnnotation", legacyName, "()Ldata/types/rename/TestEnum;"));
    }

    private static Function<RenameRule, @Nullable String> enumField(final String legacyName) {
        return renameRule -> renameRule.enumFieldRenames().get(TEST_ENUM).fieldRenames().get(legacyName);
    }
}

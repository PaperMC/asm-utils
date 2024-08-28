package io.papermc.asm.rules.classes;

import data.types.apiimpl.ApiEnum;
import data.types.apiimpl.ApiEnumImpl;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.ClassDesc;
import java.util.Map;

import static io.papermc.asm.util.DescriptorUtils.desc;

class EnumToInterfaceTest {

    private static final ClassDesc API_ENUM = desc(ApiEnum.class);
    private static final ClassDesc API_ENUM_IMPL = desc(ApiEnumImpl.class);

    @TransformerTest(value = "data.classes.EnumToInterfaceUser", copyFromClassReader = false)
    void testEnumToInterface(final TransformerCheck check) {
        final RewriteRule rule = new EnumToInterfaceRule(Map.of(API_ENUM, API_ENUM_IMPL));

        check.run(rule);
    }
}

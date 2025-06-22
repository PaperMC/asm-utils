package io.papermc.asm.versioned;

import io.papermc.asm.ApiVersions;
import io.papermc.asm.TestApiVersionImpl;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.method.MethodType;
import io.papermc.asm.rules.method.DirectStaticRewrite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TestVersionedRuleScanner {

    private static final String DUMMY_CLASS = "some.pkg.EpicClass";

    @Test
    void testVersionedRuleScanner() {
        final VersionedRuleScanner scanner = new VersionedRuleScanner(s -> new TestApiVersionImpl(Integer.parseInt(s)));

        final VersionedRuleFactory factory = scanner.scan(MethodHolder.class);

        final RewriteRule rule = factory.createRule(ApiVersions.ONE);
        assertEquals(2, ((RewriteRule.Chain) rule).rules().size());

        final RewriteRule rule2 = factory.createRule(ApiVersions.TWO);
        assertInstanceOf(DirectStaticRewrite.class, rule2);
    }

    public static final class MethodHolder {

        @DirectStaticRewrite.Wrapper(owners = DUMMY_CLASS, type = MethodType.VIRTUAL)
        @Version("1")
        public static String convert(final MethodHolder owner, final String input) {
            return input;
        }

        @Version("2")
        @DirectStaticRewrite.Wrapper(owners = DUMMY_CLASS, methodName = "getById", type = MethodType.STATIC)
        public static String notTheName(final Integer input) {
            return String.valueOf(input);
        }
    }
}

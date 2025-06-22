package io.papermc.asm.rules;

import io.papermc.asm.rules.builder.matcher.method.MethodType;
import io.papermc.asm.rules.method.DirectStaticRewrite;
import java.lang.constant.ClassDesc;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestRuleScanner {

    private static final String DUMMY_CLASS = "some.pkg.EpicClass";

    @Test
    void testRuleScanner() {
        final RewriteRule rule = RuleScanner.scan(MethodHolder.class);
        assertNotSame(RewriteRule.EMPTY, rule);
        assertInstanceOf(RewriteRule.Chain.class, rule);
        final RewriteRule.Chain chain = (RewriteRule.Chain) rule;
        assertEquals(2, chain.rules().size());

        final DirectStaticRewrite convert = (DirectStaticRewrite) chain.rules().get(0);
        assertEquals(ClassDesc.of(DUMMY_CLASS), convert.owners().iterator().next());
        final boolean matches1 = convert.methodMatcher().matches(Opcodes.INVOKEVIRTUAL, false, "convert", "(Ljava/lang/String;)Ljava/lang/String;");
        assertTrue(matches1);

        final DirectStaticRewrite notTheName = (DirectStaticRewrite) chain.rules().get(1);
        assertEquals(ClassDesc.of(DUMMY_CLASS), notTheName.owners().iterator().next());
        final boolean matches2 = notTheName.methodMatcher().matches(Opcodes.INVOKESTATIC, false, "getById", "(Ljava/lang/Integer;)Ljava/lang/String;");
        assertTrue(matches2);
    }

    @Test
    void testEmptyRuleScanner() {
        final RewriteRule rule = RuleScanner.scan(BadMethodHolder.class);
        assertSame(RewriteRule.EMPTY, rule);
    }

    public static final class MethodHolder {

        @DirectStaticRewrite.Wrapper(owners = DUMMY_CLASS, type = MethodType.VIRTUAL)
        public static String convert(final MethodHolder owner, final String input) {
            return input;
        }

        @DirectStaticRewrite.Wrapper(owners = DUMMY_CLASS, methodName = "getById", type = MethodType.STATIC)
        public static String notTheName(final Integer input) {
            return String.valueOf(input);
        }
    }

    public static final class BadMethodHolder {
        static String convert(final String input) {
            return input; // not public
        }

        public String other(final Integer input) {
            return String.valueOf(input); // not static
        }
    }
}

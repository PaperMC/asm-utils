package io.papermc.asm.rules;

import io.papermc.asm.rules.builder.matcher.method.MethodType;
import io.papermc.asm.rules.method.DirectStaticRewrite;
import java.lang.constant.ClassDesc;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class TestRuleScanner {

    private static final String DUMMY_CLASS = "some.pkg.EpicClass";

    @Test
    void testRuleScanner() {
        final RewriteRule rule = RuleScanner.scan(MethodHolder.class);
        assertNotSame(RewriteRule.EMPTY, rule);
        assertInstanceOf(RewriteRule.Chain.class, rule);
        final RewriteRule.Chain chain = (RewriteRule.Chain) rule;
        assertEquals(2, chain.rules().size());

        DirectStaticRewrite convert = null;
        DirectStaticRewrite notTheName = null;

        for (final RewriteRule r : chain.rules()) {
            final DirectStaticRewrite rewrite = (DirectStaticRewrite) r;
            assertEquals(ClassDesc.of(DUMMY_CLASS), rewrite.owners().iterator().next());

            if (rewrite.methodMatcher().matches(Opcodes.INVOKEVIRTUAL, false, "convert", "(Ljava/lang/String;)Ljava/lang/String;")) {
                convert = rewrite;
            } else if (rewrite.methodMatcher().matches(Opcodes.INVOKESTATIC, false, "getById", "(Ljava/lang/Integer;)Ljava/lang/String;")) {
                notTheName = rewrite;
            }
        }

        assertNotNull(convert, "Missing convert method rewrite");
        assertNotNull(notTheName, "Missing getById method rewrite");
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

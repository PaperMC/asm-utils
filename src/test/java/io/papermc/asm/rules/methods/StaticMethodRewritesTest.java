package io.papermc.asm.rules.methods;

import data.methods.Redirects;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import io.papermc.asm.RewriteRuleVisitorFactory;
import io.papermc.asm.TestUtil;
import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.ClassDesc;
import org.junit.jupiter.api.Test;

import static io.papermc.asm.TestUtil.testingVisitorFactory;

class StaticMethodRewritesTest {

    static final ClassDesc REDIRECTS = Redirects.class.describeConstable().orElseThrow();
    static final ClassDesc ENTITY = Entity.class.describeConstable().orElseThrow();

    @Test
    void testPlainStaticRewrite() {
        final RewriteRule renameRule = RewriteRule.forOwner(Player.class, builder -> {
            builder.plainStaticRewrite(
                REDIRECTS,
                b -> b
                    .match("addEntity", "addEntityStatic")
                    .desc(d -> d.parameterList().contains(ENTITY))
            );
        });

        final RewriteRuleVisitorFactory visitorFactory = testingVisitorFactory(renameRule);
        TestUtil.assertProcessedMatchesExpected("data/methods/statics/PlainUser", visitorFactory);
    }
}

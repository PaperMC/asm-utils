package io.papermc.asm.rules.methods;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import io.papermc.asm.RewriteRuleVisitorFactory;
import io.papermc.asm.TestUtil;
import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.ClassDesc;
import org.junit.jupiter.api.Test;

import static io.papermc.asm.TestUtil.testingVisitorFactory;

class MethodRewritesTest {

    static final ClassDesc PLAYER = Player.class.describeConstable().orElseThrow();

    @Test
    void testSuperTypeParam() {
        final RewriteRule renameRule = RewriteRule.forOwner(Methods.class, builder -> {
            builder.changeParamToSuper(
                Player.class,
                Entity.class,
                b -> b
                    .match("consume", "consumeStatic")
                    .desc(d -> d.parameterList().contains(PLAYER))
            );
        });

        final RewriteRuleVisitorFactory visitorFactory = testingVisitorFactory(renameRule);
        TestUtil.assertProcessedMatchesExpected("data/methods/inplace/SuperTypeParamUser", visitorFactory);
    }

    @Test
    void testSubTypeReturn() {
        final RewriteRule renameRule = RewriteRule.forOwner(Methods.class, builder -> {
            builder.changeReturnTypeToSub(
                Entity.class,
                Player.class,
                b -> b
                    .match("consume", "consumeStatic")
                    .desc(d -> d.returnType().equals(PLAYER))
            );
        });

        final RewriteRuleVisitorFactory visitorFactory = testingVisitorFactory(renameRule);
        TestUtil.assertProcessedMatchesExpected("data/methods/inplace/SubTypeReturnUser", visitorFactory);
    }
}

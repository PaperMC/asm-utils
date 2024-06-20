package io.papermc.asm.rules.methods;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.ClassDesc;

class MethodRewritesTest {

    static final ClassDesc PLAYER = Player.class.describeConstable().orElseThrow();
    static final ClassDesc ENTITY = Entity.class.describeConstable().orElseThrow();

    @TransformerTest("data/methods/inplace/SuperTypeParamUser")
    void testSuperTypeParam(final TransformerCheck check) {
        final RewriteRule rule = RewriteRule.forOwnerClass(Methods.class, builder -> {
            builder.changeParamToSuper(
                Player.class,
                Entity.class,
                b -> b
                    .match("consume", "consumeStatic")
                    .desc(d -> d.parameterList().contains(PLAYER))
            );
        });
        check.run(rule);
    }

    @TransformerTest("data/methods/inplace/SubTypeReturnUser")
    void testSubTypeReturn(final TransformerCheck check) {
        final RewriteRule rule = RewriteRule.forOwnerClass(Methods.class, builder -> {
            builder.changeReturnTypeToSub(
                Entity.class,
                Player.class,
                b -> b
                    .match("get", "getStatic")
                    .desc(d -> d.returnType().equals(ENTITY))
            );
        });
        check.run(rule);
    }
}

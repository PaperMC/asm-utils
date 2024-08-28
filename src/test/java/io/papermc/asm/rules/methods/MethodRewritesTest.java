package io.papermc.asm.rules.methods;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.method.MethodMatcherBuilder;
import java.lang.constant.ClassDesc;

class MethodRewritesTest {

    static final ClassDesc PLAYER = Player.class.describeConstable().orElseThrow();
    static final ClassDesc ENTITY = Entity.class.describeConstable().orElseThrow();

    @SuppressWarnings("Convert2MethodRef")
    @TransformerTest("data.methods.inplace.SuperTypeParamUser")
    void testSuperTypeParam(final TransformerCheck check) {
        final RewriteRule rule = RewriteRule.forOwnerClass(Methods.class, builder -> {
            builder.changeParamToSuper(
                Player.class,
                Entity.class,
                MethodMatcher.builder()
                    .match("consume", b -> b.virtual())
                    .match("consumeStatic", b -> b.statik())
                    .hasParam(PLAYER)
                    .build()
            );
        });
        check.run(rule);
    }

    @TransformerTest("data.methods.inplace.SubTypeReturnUser")
    void testSubTypeReturn(final TransformerCheck check) {
        final RewriteRule rule = RewriteRule.forOwnerClass(Methods.class, builder -> {
            builder.changeReturnTypeToSub(
                Entity.class,
                Player.class,
                MethodMatcher.builder()
                    .match("get", MethodMatcherBuilder.MatchBuilder::virtual)
                    .match("getStatic", MethodMatcherBuilder.MatchBuilder::statik)
                    .hasReturn(ENTITY)
                    .build()
            );
        });
        check.run(rule);
    }
}

package io.papermc.asm.rules.methods;

import data.methods.Methods;
import data.methods.Redirects;
import data.types.apiimpl.ApiInterface;
import data.types.apiimpl.ApiInterfaceImpl;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.Set;

class StaticRewriteTest {

    static final ClassDesc REDIRECTS = Redirects.class.describeConstable().orElseThrow();
    static final ClassDesc PLAYER = Player.class.describeConstable().orElseThrow();
    static final ClassDesc ENTITY = Entity.class.describeConstable().orElseThrow();

    @TransformerTest("data.methods.statics.PlainUser")
    void testPlainStaticRewrite(final TransformerCheck check) {
        final RewriteRule rule = RewriteRule.forOwnerClass(Player.class, builder -> {
            builder.plainStaticRewrite(
                REDIRECTS,
                MethodMatcher.builder()
                    .match(Set.of("addEntity", "addEntityStatic"), b -> b.hasParam(ENTITY))
                    .build()
            );
        });
        final RewriteRule ctorRule = RewriteRule.forOwnerClass(Methods.Wrapper.class, builder -> {
            builder.plainStaticRewrite(
                REDIRECTS,
                MethodMatcher.builder()
                    .ctor(b -> b.hasParam(PLAYER))
                    .build()
            );
        });
        check.run(RewriteRule.chain(rule, ctorRule));
    }

    @TransformerTest("data.methods.statics.MoveToInstanceUser")
    void testMoveInstanceMethod(final TransformerCheck check) {
        final RewriteRule rule = RewriteRule.forOwnerClass(ApiInterface.class, builder -> {
            builder.moveInstanceMethod(
                ApiInterfaceImpl.class,
                "get0",
                MethodMatcher.builder()
                    .match("get", b -> b.hasReturn(ConstantDescs.CD_String))
                    .build()
            );
        });

        check.run(rule);
    }
}

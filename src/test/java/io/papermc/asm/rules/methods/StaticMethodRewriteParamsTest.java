package io.papermc.asm.rules.methods;

import data.methods.Methods;
import data.methods.Redirects;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import data.types.hierarchy.loc.Location;
import data.types.hierarchy.loc.Position;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;

class StaticMethodRewriteParamsTest {

    static final ClassDesc REDIRECTS = Redirects.class.describeConstable().orElseThrow();
    static final ClassDesc PLAYER = Player.class.describeConstable().orElseThrow();
    static final ClassDesc ENTITY = Entity.class.describeConstable().orElseThrow();

    @TransformerTest("data.methods.statics.PlainUser")
    void testPlainStaticRewrite(final TransformerCheck check) {
        final RewriteRule rule = RewriteRule.forOwnerClass(Player.class, builder -> {
            builder.plainStaticRewrite(
                REDIRECTS,
                b -> b
                    .match("addEntity", "addEntityStatic")
                    .desc(d -> d.parameterList().contains(ENTITY))
            );
        });
        final RewriteRule ctorRule = RewriteRule.forOwnerClass(Methods.Wrapper.class, builder -> {
            builder.plainStaticRewrite(
                REDIRECTS,
                b -> b
                    .ctor()
                    .desc(d -> d.parameterList().contains(PLAYER))
            );
        });
        check.run(RewriteRule.chain(rule, ctorRule));
    }

    static final ClassDesc LOCATION = Location.class.describeConstable().orElseThrow();
    static final ClassDesc POSITION = Position.class.describeConstable().orElseThrow();

    @TransformerTest("data.methods.statics.param.ParamDirectUser")
    void testParamDirectStaticRewrite(final TransformerCheck check) throws NoSuchMethodException {
        final Method handler = Redirects.class.getDeclaredMethod("toPosition", Location.class);
        final RewriteRule rule = RewriteRule.forOwnerClass(Methods.class, builder -> {
            builder.changeParamDirect(
                POSITION,
                handler,
                b -> b.names("consumeLoc", "consumeLocStatic").containsParam(LOCATION)
            );
        });
        final RewriteRule ctorRule = RewriteRule.forOwnerClass(Methods.Wrapper.class, builder -> {
            builder.changeParamDirect(
                POSITION,
                handler,
                b -> b.ctor().containsParam(LOCATION)
            );
        });
        check.run(RewriteRule.chain(rule, ctorRule));
    }

    @TransformerTest("data.methods.statics.param.ParamFuzzyUser")
    void testParamFuzzyStaticRewrite(final TransformerCheck check) throws NoSuchMethodException {
        final Method handler = Redirects.class.getDeclaredMethod("toPositionFuzzy", Object.class);
        final RewriteRule rule = RewriteRule.forOwnerClass(Methods.class, builder -> {
            builder.changeParamFuzzy(
                POSITION,
                handler,
                b -> b.names("consumePos", "consumePosStatic").containsParam(POSITION)
            );
        });
        final RewriteRule ctorRule = RewriteRule.forOwnerClass(Methods.PosWrapper.class, builder -> {
            builder.changeParamFuzzy(
                POSITION,
                handler,
                b -> b.ctor().containsParam(POSITION)
            );
        });
        check.run(RewriteRule.chain(rule, ctorRule));
    }
}

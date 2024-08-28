package io.papermc.asm.rules.methods;

import data.methods.Methods;
import data.methods.Redirects;
import data.types.hierarchy.loc.Location;
import data.types.hierarchy.loc.Position;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;

class StaticMethodRewriteParamsTest {

    static final ClassDesc LOCATION = Location.class.describeConstable().orElseThrow();
    static final ClassDesc POSITION = Position.class.describeConstable().orElseThrow();

    @SuppressWarnings("Convert2MethodRef")
    @TransformerTest("data.methods.statics.param.ParamDirectUser")
    void testParamDirectStaticRewrite(final TransformerCheck check) throws NoSuchMethodException {
        final Method handler = Redirects.class.getDeclaredMethod("toPosition", Location.class);
        final RewriteRule rule = RewriteRule.forOwnerClass(Methods.class, builder ->
            builder.changeParamDirect(
                POSITION,
                handler,
                TargetedMethodMatcher.builder()
                    .match("consumeLoc", b -> b.virtual())
                    .match("consumeLocStatic", b -> b.statik())
                    .hasParam(LOCATION)
                    .build()
            )
        );
        final RewriteRule ctorRule = RewriteRule.forOwnerClass(Methods.Wrapper.class, builder -> {
            builder.changeParamDirect(
                POSITION,
                handler,
                TargetedMethodMatcher.builder().ctor().hasParam(LOCATION).build()
            );
        });
        check.run(RewriteRule.chain(rule, ctorRule));
    }

    @SuppressWarnings("Convert2MethodRef")
    @TransformerTest("data.methods.statics.param.ParamFuzzyUser")
    void testParamFuzzyStaticRewrite(final TransformerCheck check) throws NoSuchMethodException {
        final Method handler = Redirects.class.getDeclaredMethod("toPositionFuzzy", Object.class);
        final RewriteRule rule = RewriteRule.forOwnerClass(Methods.class, builder -> {
            builder.changeParamFuzzy(
                POSITION,
                handler,
                TargetedMethodMatcher.builder()
                    .match("consumePos", b -> b.virtual())
                    .match("consumePosStatic", b -> b.statik())
                    .hasParam(POSITION)
                    .build()
            );
        });
        final RewriteRule ctorRule = RewriteRule.forOwnerClass(Methods.PosWrapper.class, builder -> {
            builder.changeParamFuzzy(
                POSITION,
                handler,
                TargetedMethodMatcher.builder().ctor().hasParam(POSITION).build()
            );
        });
        check.run(RewriteRule.chain(rule, ctorRule));
    }
}

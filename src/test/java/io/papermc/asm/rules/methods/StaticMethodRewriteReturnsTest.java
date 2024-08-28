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

class StaticMethodRewriteReturnsTest {

    static final ClassDesc LOCATION = Location.class.describeConstable().orElseThrow();
    static final ClassDesc POSITION = Position.class.describeConstable().orElseThrow();

    @SuppressWarnings("Convert2MethodRef")
    @TransformerTest("data.methods.statics.returns.ReturnDirectUser")
    void testReturnDirectStaticRewrite(final TransformerCheck check) throws NoSuchMethodException {
        final Method handler = Redirects.class.getDeclaredMethod("wrapPosition", Position.class);
        final RewriteRule rule = RewriteRule.forOwnerClass(Methods.class, builder -> {
            builder.changeReturnTypeDirect(
                POSITION,
                handler,
                TargetedMethodMatcher.builder()
                    .match("getLoc", b -> b.virtual())
                    .match("getLocStatic", b -> b.statik())
                    .targetReturn(LOCATION)
                    .build()
            );
        });
        check.run(rule);
    }

    @SuppressWarnings("Convert2MethodRef")
    @TransformerTest("data.methods.statics.returns.ReturnDirectWithContextUser")
    void testReturnDirectWithContextStaticRewrite(final TransformerCheck check) throws NoSuchMethodException {
        final Method handler = Redirects.class.getDeclaredMethod("wrapPositionWithContext", Methods.class, Position.class);
        final RewriteRule rule = RewriteRule.forOwnerClass(Methods.class, builder -> {
            builder.changeReturnTypeDirectWithContext(
                POSITION,
                handler,
                TargetedMethodMatcher.builder()
                    .match("getLoc", b -> b.virtual())
                    .match("getLocStatic", b -> b.statik())
                    .targetReturn(LOCATION)
                    .build()
            );
        });
        check.run(rule);
    }
}

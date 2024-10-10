package io.papermc.asm.rules.methods;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import io.papermc.asm.ApiVersion;
import io.papermc.asm.TransformerTest;
import io.papermc.asm.checks.TransformerCheck;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.rules.builder.matcher.method.MethodMatcherBuilder;
import io.papermc.asm.rules.method.params.SuperTypeParamRewrite;
import io.papermc.asm.rules.method.returns.SubTypeReturnRewrite;
import io.papermc.asm.versioned.VersionedRuleFactory;
import io.papermc.asm.versioned.VersionedTester;
import io.papermc.asm.versioned.matcher.VersionedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.Map;
import org.junit.jupiter.api.Test;

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

    @Test
    void testVersionedSuperTypeParam() {
        final VersionedRuleFactory factory = VersionedRuleFactory.forOwnerClass(String.class, builder -> {
            final MethodMatcher method1 = MethodMatcher.builder().match("method1").build();
            builder.changeParamToSuper(
                String.class,
                VersionedMethodMatcher.builder()
                    .with(ApiVersion.ONE, method1, ConstantDescs.CD_int)
                    .with(ApiVersion.THREE, method1, ConstantDescs.CD_long)
                    .build()
            );
        });

        final VersionedTester tester = new VersionedTester(factory, ApiVersion.ALL_VERSIONS);
        tester.test(SuperTypeParamRewrite::oldParamType, Map.of(
            ApiVersion.ONE, ConstantDescs.CD_int,
            ApiVersion.THREE, ConstantDescs.CD_long
        ));
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

    @Test
    void testVersionedSubTypeReturn() {
        final VersionedRuleFactory factory = VersionedRuleFactory.forOwnerClass(String.class, builder -> {
            final MethodMatcher method1 = MethodMatcher.builder().match("method1").build();
            builder.changeReturnTypeToSub(
                String.class,
                VersionedMethodMatcher.builder()
                    .with(ApiVersion.ONE, method1, ConstantDescs.CD_int)
                    .with(ApiVersion.THREE, method1, ConstantDescs.CD_long)
                    .build()
            );
        });

        final VersionedTester tester = new VersionedTester(factory, ApiVersion.ALL_VERSIONS);
        tester.test(SubTypeReturnRewrite::oldReturnType, Map.of(
            ApiVersion.ONE, ConstantDescs.CD_int,
            ApiVersion.THREE, ConstantDescs.CD_long
        ));
    }
}

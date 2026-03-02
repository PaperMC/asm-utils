package io.papermc.classfile.method.action;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import io.papermc.classfile.RewriteProcessor;
import io.papermc.classfile.TransformerTest;
import io.papermc.classfile.checks.TransformerCheck;
import io.papermc.classfile.method.MethodDescriptorPredicate;
import io.papermc.classfile.method.MethodNamePredicate;
import io.papermc.classfile.method.MethodRewrite;
import java.lang.constant.ClassDesc;
import java.util.List;

import static io.papermc.classfile.ClassFiles.desc;

class TestSubtypeReturn {

    static final ClassDesc METHODS = desc(Methods.class);
    static final ClassDesc ENTITY = desc(Entity.class);
    static final ClassDesc PLAYER = desc(Player.class);

    @TransformerTest("data.methods.inplace.SubTypeReturnUser")
    void test(final TransformerCheck check) {
        final List<MethodRewrite> rewrites = List.of(
            new MethodRewrite(METHODS, MethodNamePredicate.exact("get", "getStatic"), MethodDescriptorPredicate.hasReturn(ENTITY), new SubtypeReturn(PLAYER))
        );
        check.run(new RewriteProcessor(rewrites));
    }
}

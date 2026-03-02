package io.papermc.classfile.method.action;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import io.papermc.classfile.RewriteProcessor;
import io.papermc.classfile.TransformerTest;
import io.papermc.classfile.checks.TransformerCheck;
import io.papermc.classfile.method.MethodRewrite;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

import static io.papermc.classfile.ClassFiles.desc;
import static io.papermc.classfile.method.MethodDescriptorPredicate.hasParameter;
import static io.papermc.classfile.method.MethodNamePredicate.exact;

class TestSupertypeParam {

    static final ClassDesc METHODS = desc(Methods.class);
    static final ClassDesc PLAYER = desc(Player.class);
    static final ClassDesc ENTITY = desc(Entity.class);

    @TransformerTest("data.methods.inplace.SuperTypeParamUser")
    void testSuperTypeParameter(final TransformerCheck check) {
        final List<String> methodNames = List.of("consume", "consumeStatic");
        final List<MethodRewrite> rewrites = new ArrayList<>();
        for (final String name : methodNames) {
            rewrites.add(new MethodRewrite(METHODS, exact(name), hasParameter(PLAYER), new SupertypeParam(ENTITY)));
        }

        check.run(new RewriteProcessor(rewrites));
    }
}

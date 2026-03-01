package io.papermc.classfile;

import data.methods.Methods;
import data.methods.Redirects;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import io.papermc.classfile.checks.TransformerCheck;
import io.papermc.classfile.method.MethodDescriptorPredicate;
import io.papermc.classfile.method.MethodNamePredicate;
import io.papermc.classfile.method.MethodRewrite;
import io.papermc.classfile.method.action.DirectStaticCall;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

import static io.papermc.classfile.ClassfileUtils.desc;

class SimpleTest {

    static final ClassDesc PLAYER = desc(Player.class);
    static final ClassDesc ENTITY = desc(Entity.class);
    static final ClassDesc METHODS_WRAPPER = desc(Methods.Wrapper.class);

    static final ClassDesc NEW_OWNER = desc(Redirects.class);

    @TransformerTest("data.methods.statics.PlainUser")
    void test(final TransformerCheck check) {
        final List<MethodRewrite> rewriteList = new ArrayList<>();
        final List<String> methodNames = List.of("addEntity", "addEntityStatic", "addEntityAndPlayer", "addEntityAndPlayerStatic");
        for (final String methodName : methodNames) {
            rewriteList.add(new MethodRewrite(PLAYER, MethodNamePredicate.exact(methodName), MethodDescriptorPredicate.hasParameter(ENTITY), new DirectStaticCall(NEW_OWNER)));
        }

        rewriteList.add(new MethodRewrite(METHODS_WRAPPER, MethodNamePredicate.constructor(), MethodDescriptorPredicate.hasParameter(PLAYER), new DirectStaticCall(NEW_OWNER)));
        final RewriteProcessor rewriteProcessor = new RewriteProcessor(rewriteList);
        check.run(rewriteProcessor);
    }
}

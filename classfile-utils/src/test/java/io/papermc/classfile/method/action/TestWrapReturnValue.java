package io.papermc.classfile.method.action;

import data.methods.Methods;
import data.methods.Redirects;
import data.types.hierarchy.loc.Location;
import data.types.hierarchy.loc.Position;
import io.papermc.classfile.RewriteProcessor;
import io.papermc.classfile.TransformerTest;
import io.papermc.classfile.checks.TransformerCheck;
import io.papermc.classfile.method.MethodDescriptorPredicate;
import io.papermc.classfile.method.MethodNamePredicate;
import io.papermc.classfile.method.MethodRewrite;
import java.lang.constant.ClassDesc;
import java.util.List;

import static io.papermc.classfile.ClassFiles.desc;

class TestWrapReturnValue {

    static final ClassDesc METHODS = desc(Methods.class);
    static final ClassDesc REDIRECTS = desc(Redirects.class);
    static final ClassDesc LOCATION = desc(Location.class);
    static final ClassDesc POSITION = desc(Position.class);

    @TransformerTest("data.methods.statics.returns.ReturnDirectUser")
    void test(final TransformerCheck check) {
        final List<MethodRewrite> rewrites = List.of(
            new MethodRewrite(METHODS, MethodNamePredicate.exact("getLoc", "getLocStatic"), MethodDescriptorPredicate.hasReturn(LOCATION), new WrapReturnValue(REDIRECTS, "wrapPosition", POSITION))
        );
        check.run(new RewriteProcessor(rewrites));
    }
}

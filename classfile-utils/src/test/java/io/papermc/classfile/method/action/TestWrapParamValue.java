package io.papermc.classfile.method.action;

import data.methods.Methods;
import data.methods.Redirects;
import data.types.hierarchy.loc.Location;
import data.types.hierarchy.loc.Position;
import io.papermc.classfile.RewriteProcessor;
import io.papermc.classfile.TransformerTest;
import io.papermc.classfile.checks.TransformerCheck;
import io.papermc.classfile.method.MethodRewrite;
import java.lang.constant.ClassDesc;
import java.util.List;

import static io.papermc.classfile.ClassFiles.desc;
import static io.papermc.classfile.method.MethodDescriptorPredicate.hasParameter;
import static io.papermc.classfile.method.MethodNamePredicate.constructor;
import static io.papermc.classfile.method.MethodNamePredicate.exact;

class TestWrapParamValue {

    static final ClassDesc METHODS = desc(Methods.class);
    static final ClassDesc METHODS_WRAPPER = desc(Methods.Wrapper.class);
    static final ClassDesc REDIRECTS = desc(Redirects.class);
    static final ClassDesc LOCATION = desc(Location.class);
    static final ClassDesc POSITION = desc(Position.class);

    @TransformerTest("data.methods.statics.param.ParamDirectUser")
    void testWrapParamValue(final TransformerCheck check) {
        final WrapParamValue toPositionAction = new WrapParamValue(REDIRECTS, "toPosition", POSITION);
        final List<MethodRewrite> rewrites = List.of(
            new MethodRewrite(METHODS, exact("consumeLoc", "consumeLocStatic"), hasParameter(LOCATION), toPositionAction),
            new MethodRewrite(METHODS_WRAPPER, constructor(), hasParameter(LOCATION), toPositionAction)
        );
        check.run(new RewriteProcessor(rewrites));
    }
}

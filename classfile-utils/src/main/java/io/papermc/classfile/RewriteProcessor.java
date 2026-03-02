package io.papermc.classfile;

import io.papermc.classfile.method.MethodRewrite;
import io.papermc.classfile.method.MethodRewriteIndex;
import io.papermc.classfile.method.transform.BridgeMethodRegistry;
import io.papermc.classfile.transform.TransformContext;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.util.List;

public class RewriteProcessor {

    private static final ClassFile CLASS_FILE = ClassFile.of();

    private final MethodRewriteIndex methodIndex;

    public RewriteProcessor(final List<MethodRewrite> methodRewrites) {
        this.methodIndex = new MethodRewriteIndex(methodRewrites);
    }

    public byte[] rewrite(final byte[] input) {
        final ClassModel inputModel = CLASS_FILE.parse(input);
        final BridgeMethodRegistry bridges = new BridgeMethodRegistry();
        final TransformContext context = TransformContext.create(inputModel.thisClass().asSymbol(), bridges);
        final ClassTransform transform = ClassTransform.transformingMethods(MethodRewrite.createTransform(this.methodIndex, context))
            .andThen(ClassTransform.endHandler(bridges::emitAll));
        return CLASS_FILE.transformClass(inputModel, transform);
    }

}

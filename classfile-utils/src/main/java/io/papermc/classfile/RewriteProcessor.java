package io.papermc.classfile;

import io.papermc.classfile.method.MethodRewrite;
import io.papermc.classfile.method.MethodRewriteIndex;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.util.List;

public class RewriteProcessor {

    private static final ClassFile CLASS_FILE = ClassFile.of();

    private final ClassTransform transform;

    public RewriteProcessor(final List<MethodRewrite> methodRewrites) {
        final MethodRewriteIndex methodIndex = new MethodRewriteIndex(methodRewrites);
        this.transform = MethodRewrite.createTransform(methodIndex);
    }

    public byte[] rewrite(final byte[] input) {
        final ClassModel inputModel = CLASS_FILE.parse(input);
        return CLASS_FILE.transformClass(inputModel, this.transform);
    }
}

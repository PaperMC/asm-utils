package io.papermc.classfile;

import io.papermc.classfile.method.MethodRewrite;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.util.List;
import java.util.function.Predicate;

public class RewriteProcessor {

    private static final ClassFile CLASS_FILE = ClassFile.of();

    private final List<MethodRewrite> ctorRewrites;

    private final CodeTransform normalTransform;
    private final ClassTransform transform;

    public RewriteProcessor(final List<MethodRewrite> methodRewrites) {
        this.ctorRewrites = methodRewrites.stream().filter(MethodRewrite::requiresMethodTransform).toList();
        final List<MethodRewrite> normalRewrites = methodRewrites.stream().filter(Predicate.not(MethodRewrite::requiresMethodTransform)).toList();

        // TODO look into this andThen issue. I think it causes way too many calls.
        // it seems to send the corrected instruction through all the other transforms as well.
        this.normalTransform = normalRewrites.stream().reduce(CodeTransform.ACCEPT_ALL, CodeTransform::andThen, CodeTransform::andThen);
        this.transform = this.buildTransform();
    }

    private ClassTransform buildTransform() {
        if (this.ctorRewrites.isEmpty()) {
            return ClassTransform.transformingMethodBodies(this.normalTransform);
        } else {
            return (classBuilder, classElement) -> {
                if (classElement instanceof final MethodModel method) {
                    classBuilder.transformMethod(method, (methodBuilder, methodElement) -> {
                        if (methodElement instanceof final CodeModel code) {
                            final CodeTransform perMethod = this.ctorRewrites.stream()
                                .map(MethodRewrite::newConstructorTransform)
                                .reduce(this.normalTransform, CodeTransform::andThen);
                            methodBuilder.transformCode(code, perMethod);
                        } else {
                            methodBuilder.with(methodElement);
                        }
                    });
                } else {
                    classBuilder.with(classElement);
                }
            };
        }
    }

    public byte[] rewrite(final byte[] input) {
        final ClassModel inputModel = CLASS_FILE.parse(input);
        return CLASS_FILE.transformClass(inputModel, this.transform);
    }
}

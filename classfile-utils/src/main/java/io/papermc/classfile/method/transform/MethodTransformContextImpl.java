package io.papermc.classfile.method.transform;

import io.papermc.classfile.method.MethodRewrite;
import java.lang.classfile.CodeElement;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.constant.ClassDesc;

record MethodTransformContextImpl(
    ClassDesc currentClass,
    BridgeMethodRegistry bridges,
    ConstantPoolBuilder constantPool,
    MethodInfo methodInfo,
    TrackingConsumer<CodeElement> emit,
    MethodRewrite currentRewrite
) implements MethodTransformContext {

    @Override
    public void emit(final CodeElement element) {
        this.emit.accept(element);
    }

    @Override
    public TrackingConsumer<CodeElement>.Instance prime() {
        return this.emit.prime();
    }
}

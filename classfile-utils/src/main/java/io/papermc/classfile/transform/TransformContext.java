package io.papermc.classfile.transform;

import io.papermc.classfile.method.transform.BridgeMethodRegistry;
import java.lang.constant.ClassDesc;

public interface TransformContext {

    static TransformContext create(final ClassDesc currentClass, final BridgeMethodRegistry bridges) {
        return new TransformContextImpl(currentClass, bridges);
    }

    ClassDesc currentClass();

    BridgeMethodRegistry bridges();
}

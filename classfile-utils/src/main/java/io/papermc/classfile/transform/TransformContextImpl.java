package io.papermc.classfile.transform;

import io.papermc.classfile.method.transform.BridgeMethodRegistry;
import java.lang.constant.ClassDesc;

record TransformContextImpl(ClassDesc currentClass, BridgeMethodRegistry bridges) implements TransformContext {
}

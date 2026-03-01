package io.papermc.classfile.method.action;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.Opcode;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.function.Consumer;

public sealed interface MethodRewriteAction permits DirectStaticCall {

    int BOOTSTRAP_HANDLE_IDX = 1;
    int DYNAMIC_TYPE_IDX = 2;
    String CONSTRUCTOR_METHOD_NAME = "<init>";

    void rewriteInvoke(Consumer<CodeElement> emit, ConstantPoolBuilder poolBuilder, Opcode opcode, ClassDesc owner, String name, MethodTypeDesc descriptor);

    void rewriteInvokeDynamic(CodeBuilder builder, DirectMethodHandleDesc.Kind kind, ClassDesc owner, String name, MethodTypeDesc descriptor, BootstrapInfo bootstrapInfo);

    record BootstrapInfo(DirectMethodHandleDesc method, String invocationName, MethodTypeDesc invocationType, List<ConstantDesc> args) {

        DynamicCallSiteDesc create(final ConstantDesc[] newArgs) {
            return DynamicCallSiteDesc.of(this.method, this.invocationName, this.invocationType, newArgs);
        }
    }

}

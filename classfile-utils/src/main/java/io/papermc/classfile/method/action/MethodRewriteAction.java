package io.papermc.classfile.method.action;

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

    /**
     * Rewrites a method invocation instruction, modifying the method owner,
     * name, and descriptor, and emits the modified instruction.
     *
     * @param emit A consumer that accepts the newly created {@code CodeElement}.
     *             This is used to emit the rewritten instruction. **MUST** be called or exception thrown.
     * @param poolBuilder The {@code ConstantPoolBuilder} used to manage and create
     *                    constant pool entries required by the instruction.
     * @param opcode The original {@code Opcode} of the method invocation
     *               (e.g., {@code INVOKEVIRTUAL}, {@code INVOKESTATIC}).
     * @param owner The {@code ClassDesc} representing the class that owns the original method.
     * @param name The name of the method being invoked.
     * @param descriptor The {@code MethodTypeDesc} describing the method's parameters
     *                   and return type.
     */
    void rewriteInvoke(Consumer<CodeElement> emit, ConstantPoolBuilder poolBuilder, Opcode opcode, ClassDesc owner, String name, MethodTypeDesc descriptor);

    /**
     * Rewrites an invokedynamic instruction, modifying its bootstrap method,
     * method owner, method name, and method descriptor, then emits the modified instruction.
     * The bootstrap method arguments and type are defined in the {@code BootstrapInfo}.
     *
     * @param emit A consumer that accepts the newly created {@code CodeElement}.
     *             This is used to emit the rewritten invokedynamic instruction.
     *             **MUST** be called or an exception should be thrown.
     * @param poolBuilder The {@code ConstantPoolBuilder} used to manage and create
     *                    constant pool entries required by the invokedynamic instruction.
     * @param kind The {@code DirectMethodHandleDesc.Kind} indicating the kind of method handle
     *             associated with the bootstrap method.
     * @param owner The {@code ClassDesc} representing the class that owns the invokedynamic call site.
     * @param name The name of the method or call site being referenced by the invokedynamic instruction.
     * @param descriptor The {@code MethodTypeDesc} describing the parameters and return type of the method.
     * @param bootstrapInfo An instance of {@code BootstrapInfo} containing details about the bootstrap method,
     *                      including its method handle, invocation name and type, and additional arguments.
     */
    void rewriteInvokeDynamic(Consumer<CodeElement> emit, ConstantPoolBuilder poolBuilder, DirectMethodHandleDesc.Kind kind, ClassDesc owner, String name, MethodTypeDesc descriptor, BootstrapInfo bootstrapInfo);

    record BootstrapInfo(DirectMethodHandleDesc method, String invocationName, MethodTypeDesc invocationType, List<ConstantDesc> args) {

        DynamicCallSiteDesc create(final ConstantDesc[] newArgs) {
            return DynamicCallSiteDesc.of(this.method, this.invocationName, this.invocationType, newArgs);
        }
    }

}

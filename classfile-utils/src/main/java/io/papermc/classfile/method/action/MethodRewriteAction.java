package io.papermc.classfile.method.action;

import io.papermc.classfile.method.MethodDescriptorPredicate;
import io.papermc.classfile.method.MethodNamePredicate;
import io.papermc.classfile.method.MethodRewrite;
import io.papermc.classfile.method.transform.MethodTransformContext;
import java.lang.classfile.Opcode;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Optional;

public sealed interface MethodRewriteAction permits DirectStaticCall, SubtypeReturn, WrapReturnValue {

    /**
     * Check that the specified rewrite is configured correctly for this action.
     *
     * @param namePredicate the name predicate to check
     * @param descriptorPredicate the descriptor predicate to check
     * @return empty optional if valid, error message otherwise
     */
    Optional<String> isValidFor(MethodNamePredicate namePredicate, MethodDescriptorPredicate descriptorPredicate);

    /**
     * Rewrites a method invocation instruction, modifying the method owner,
     * name, and descriptor, and emits the modified instruction.
     *
     * @param context The context containing information about the method invocation.
     * @param opcode The opcode of the method invocation instruction.
     */
    void rewriteInvoke(MethodTransformContext context, Opcode opcode);

    /**
     * Rewrites an invokedynamic instruction, modifying its bootstrap method,
     * method owner, method name, and method descriptor, then emits the modified instruction.
     * The bootstrap method arguments and type are defined in the {@code BootstrapInfo}.
     *
     * @param context The context containing information about the invokedynamic instruction.
     * @param kind The {@code DirectMethodHandleDesc.Kind} indicating the kind of method handle
     *             associated with the bootstrap method.
     * @param bootstrapInfo An instance of {@code BootstrapInfo} containing details about the bootstrap method,
     *                      including its method handle, invocation name and type, and additional arguments.
     */
    void rewriteInvokeDynamic(MethodTransformContext context, DirectMethodHandleDesc.Kind kind, BootstrapInfo bootstrapInfo);

    record BootstrapInfo(DirectMethodHandleDesc method, String invocationName, MethodTypeDesc invocationType, List<ConstantDesc> args) {

        DynamicCallSiteDesc create(final ConstantDesc[] newArgs) {
            return DynamicCallSiteDesc.of(this.method, this.invocationName, this.invocationType, newArgs);
        }
    }

}

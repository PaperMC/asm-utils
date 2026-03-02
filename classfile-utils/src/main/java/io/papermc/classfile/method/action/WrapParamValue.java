package io.papermc.classfile.method.action;

import io.papermc.classfile.generation.ParameterGeneration;
import io.papermc.classfile.method.MethodDescriptorPredicate;
import io.papermc.classfile.method.MethodNamePredicate;
import io.papermc.classfile.method.transform.MethodTransformContext;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Optional;

import static io.papermc.classfile.ClassFiles.BOOTSTRAP_HANDLE_IDX;
import static io.papermc.classfile.ClassFiles.adjustForStatic;
import static io.papermc.classfile.ClassFiles.constructorMethodName;
import static io.papermc.classfile.ClassFiles.emitInvoke;
import static io.papermc.classfile.ClassFiles.replaceParameters;
import static java.util.function.Predicate.isEqual;

public record WrapParamValue(ClassDesc converterOwner, String converterMethod, ClassDesc newParamType) implements MethodRewriteAction {

    @Override
    public Optional<String> isValidFor(final MethodNamePredicate namePredicate, final MethodDescriptorPredicate descriptorPredicate) {
        if (!(descriptorPredicate instanceof MethodDescriptorPredicate.HasParameter)) {
            return Optional.of("You must use a parameter descriptor predicate on " + descriptorPredicate);
        }
        return Optional.empty();
    }

    @Override
    public void rewriteInvoke(final MethodTransformContext context, final Opcode opcode) {
        final MethodTransformContext.MethodInfo info = context.methodInfo();
        final ClassDesc targetParamType = context.currentRewrite().descriptor().targetType();
        final MethodTypeDesc callDesc = replaceParameters(info.descriptor(), isEqual(targetParamType), this.newParamType());
        final MethodTypeDesc staticReplacement = adjustForStatic(opcode, info.owner(), info.descriptor());
        // descriptor for converting the old param type to the new param type
        final MethodTypeDesc converterDesc = MethodTypeDesc.of(this.newParamType(), targetParamType);

        final String bridgeMethodName;
        if (opcode == Opcode.INVOKESPECIAL) {
            bridgeMethodName = constructorMethodName(info.owner());
        } else {
            bridgeMethodName = info.name();
        }
        final String bridgeName = context.bridges().registerBridge(
            info.owner(),
            bridgeMethodName,
            staticReplacement,
            ParameterGeneration.mutating(
                cb -> {
                    if (opcode == Opcode.INVOKESPECIAL) {
                        cb.new_(info.owner());
                        cb.dup();
                    }
                },
                (paramType, builder) -> {
                    if (paramType.equals(targetParamType)) {
                        builder.invokestatic(this.converterOwner(), this.converterMethod(), converterDesc);
                    }
                }),
            cb -> emitInvoke(cb, opcode, info, callDesc, true)
        );

        context.emitToBridgeMethod(bridgeName, staticReplacement);
    }

    @Override
    public void rewriteInvokeDynamic(final MethodTransformContext context, final DirectMethodHandleDesc.Kind kind, final BootstrapInfo bootstrapInfo) {
        final MethodTransformContext.MethodInfo info = context.methodInfo();
        final ClassDesc targetParamType = context.currentRewrite().descriptor().targetType();
        final MethodTypeDesc callDesc = replaceParameters(info.descriptor(), isEqual(targetParamType), this.newParamType());
        final MethodTypeDesc staticReplacement = adjustForStatic(kind, info.owner(), info.descriptor());
        // descriptor for converting the old param type to the new param type
        final MethodTypeDesc converterDesc = MethodTypeDesc.of(this.newParamType(), targetParamType);

        final String bridgeMethodName;
        if (kind == DirectMethodHandleDesc.Kind.CONSTRUCTOR) {
            bridgeMethodName = constructorMethodName(info.owner());
        } else {
            bridgeMethodName = info.name();
        }
        final String bridgeName = context.bridges().registerBridge(
            info.owner(),
            bridgeMethodName,
            staticReplacement,
            ParameterGeneration.mutating(
                cb -> {
                    if (kind == DirectMethodHandleDesc.Kind.CONSTRUCTOR) {
                        cb.new_(info.owner());
                        cb.dup();
                    }
                },
                (paramType, builder) -> {
                    if (paramType.equals(targetParamType)) {
                        builder.invokestatic(this.converterOwner(), this.converterMethod(), converterDesc);
                    }
                }
            ),
            cb -> emitInvoke(cb, kind, info, callDesc, true)
        );

        // Redirect the invokedynamic to the bridge; arg[2] (instantiated type) stays the same
        // because the bridge preserves the original return type.
        final ConstantDesc[] newArgs = bootstrapInfo.args().toArray(new ConstantDesc[0]);
        newArgs[BOOTSTRAP_HANDLE_IDX] = context.createBridgeHandle(bridgeName, staticReplacement);
        context.emit(InvokeDynamicInstruction.of(context.constantPool().invokeDynamicEntry(bootstrapInfo.create(newArgs))));
    }
}

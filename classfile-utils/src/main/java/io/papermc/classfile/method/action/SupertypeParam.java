package io.papermc.classfile.method.action;

import io.papermc.classfile.ClassFiles;
import io.papermc.classfile.method.MethodDescriptorPredicate;
import io.papermc.classfile.method.MethodNamePredicate;
import io.papermc.classfile.method.transform.MethodTransformContext;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Optional;

import static io.papermc.classfile.ClassFiles.BOOTSTRAP_HANDLE_IDX;
import static io.papermc.classfile.ClassFiles.replaceParameters;
import static java.util.function.Predicate.isEqual;

public record SupertypeParam(ClassDesc newParamType) implements MethodRewriteAction {

    @Override
    public Optional<String> isValidFor(final MethodNamePredicate namePredicate, final MethodDescriptorPredicate descriptorPredicate) {
        // only valid if you are search by return type
        if (!(descriptorPredicate instanceof MethodDescriptorPredicate.HasParameter)) {
            return Optional.of("You must use a parameter descriptor predicate on " + descriptorPredicate);
        }
        return Optional.empty();
    }

    @Override
    public void rewriteInvoke(final MethodTransformContext context, final Opcode opcode) {
        final MethodTransformContext.MethodInfo info = context.methodInfo();
        final ClassDesc targetParamType = context.currentRewrite().descriptor().targetType();
        final MethodTypeDesc newDescriptor = replaceParameters(info.descriptor(), isEqual(targetParamType), this.newParamType());
        context.emitChangedDescriptor(opcode, newDescriptor);
    }

    @Override
    public void rewriteInvokeDynamic(final MethodTransformContext context, final DirectMethodHandleDesc.Kind kind, final BootstrapInfo bootstrapInfo) {
        final MethodTransformContext.MethodInfo info = context.methodInfo();
        final ClassDesc targetParamType = context.currentRewrite().descriptor().targetType();
        final ConstantDesc[] newArgs = bootstrapInfo.args().toArray(new ConstantDesc[0]);
        final MethodTypeDesc newDescriptor = replaceParameters(info.descriptor(), isEqual(targetParamType), this.newParamType());
        newArgs[BOOTSTRAP_HANDLE_IDX] = MethodHandleDesc.ofMethod(kind, info.owner(), info.name(), newDescriptor);
        // we are changing the descriptor directly instead of delegating, so we need to change the dynamic type
        if (newArgs[ClassFiles.DYNAMIC_TYPE_IDX] instanceof final MethodTypeDesc instantiatedType) {
            newArgs[ClassFiles.DYNAMIC_TYPE_IDX] = replaceParameters(instantiatedType, isEqual(targetParamType), this.newParamType());
        }
        context.emit(InvokeDynamicInstruction.of(context.constantPool().invokeDynamicEntry(bootstrapInfo.create(newArgs))));
    }
}

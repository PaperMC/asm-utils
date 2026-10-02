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

/**
 * A {@link MethodRewriteAction} that changes the return type of a method invocation to a subtype,
 * without inserting any conversion call. The return type in the method descriptor is updated to
 * {@code newReturnType}, and no additional instructions are emitted. This is valid when the new
 * API returns a subtype that is assignment-compatible with the old return type.
 *
 * @param newReturnType The subtype to use as the new return type in the rewritten descriptor.
 */
public record SubtypeReturn(ClassDesc newReturnType) implements MethodRewriteAction {

    @Override
    public Optional<String> isValidFor(final MethodNamePredicate namePredicate, final MethodDescriptorPredicate descriptorPredicate) {
        // only valid if you are search by return type
        if (!(descriptorPredicate instanceof MethodDescriptorPredicate.HasReturn)) {
            return Optional.of("You must use a return descriptor predicate on " + descriptorPredicate);
        }
        return Optional.empty();
    }

    @Override
    public void rewriteInvoke(final MethodTransformContext context, final Opcode opcode) {
        final MethodTransformContext.MethodInfo info = context.methodInfo();
        final MethodTypeDesc newDescriptor = info.descriptor().changeReturnType(this.newReturnType);
        context.emitChangedDescriptor(opcode, newDescriptor);
    }

    @Override
    public void rewriteInvokeDynamic(final MethodTransformContext context, final DirectMethodHandleDesc.Kind kind, final BootstrapInfo bootstrapInfo) {
        final MethodTransformContext.MethodInfo info = context.methodInfo();
        final ConstantDesc[] newArgs = bootstrapInfo.args().toArray(new ConstantDesc[0]);
        final MethodTypeDesc handleMethodType = info.descriptor().changeReturnType(this.newReturnType);
        newArgs[ClassFiles.BOOTSTRAP_HANDLE_IDX] = MethodHandleDesc.ofMethod(kind, info.owner(), info.name(), handleMethodType);
        // we are changing the descriptor directly instead of delegating, so we need to change the dynamic type
        if (newArgs[ClassFiles.DYNAMIC_TYPE_IDX] instanceof final MethodTypeDesc instantiatedType) {
            newArgs[ClassFiles.DYNAMIC_TYPE_IDX] = instantiatedType.changeReturnType(this.newReturnType);
        }
        context.emit(InvokeDynamicInstruction.of(context.constantPool().invokeDynamicEntry(bootstrapInfo.create(newArgs))));
    }
}

package io.papermc.classfile.method.action;

import io.papermc.classfile.ClassFiles;
import io.papermc.classfile.method.MethodDescriptorPredicate;
import io.papermc.classfile.method.MethodNamePredicate;
import io.papermc.classfile.method.transform.MethodTransformContext;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Optional;

/**
 * A {@link MethodRewriteAction} that intercepts a method invocation, emitting a call to the
 * updated method (whose return type changed to {@code newReturnType}), followed by a static
 * converter call that converts the new return type back to the original return type expected
 * by old code.
 *
 * <p>For invokedynamic (lambdas/method references), a synthetic bridge method is generated
 * in the class being transformed. The bridge calls the updated method and applies the converter,
 * then the invokedynamic is redirected to the bridge.</p>
 *
 * @param converterOwner  The class that owns the static converter method.
 * @param converterMethod The name of the static converter method, which must accept
 *                        {@code newReturnType} and return the original return type.
 * @param newReturnType   The return type introduced by the new API.
 */
public record WrapReturnValue(ClassDesc converterOwner, String converterMethod, ClassDesc newReturnType) implements MethodRewriteAction {

    @Override
    public Optional<String> isValidFor(final MethodNamePredicate namePredicate, final MethodDescriptorPredicate descriptorPredicate) {
        // only valid if you are search by return type
        if (!(descriptorPredicate instanceof MethodDescriptorPredicate.ReturnType)) {
            return Optional.of("You must use a return descriptor predicate on " + descriptorPredicate);
        }
        // TODO maybe this works???
        // if (namePredicate instanceof MethodNamePredicate.Constructor) {
        //     return Optional.of("Cannot wrap return value of constructor");
        // }
        return Optional.empty();
    }

    @Override
    public void rewriteInvoke(final MethodTransformContext context, final Opcode opcode) {
        final MethodTransformContext.MethodInfo info = context.methodInfo();
        // Emit the original call with the new return type
        final MethodTypeDesc callDesc = info.descriptor().changeReturnType(this.newReturnType);
        context.emitChangedDescriptor(opcode, callDesc);
        // Emit the converter: converterOwner.converterMethod(newReturnType) -> original return type
        final MethodTypeDesc converterDesc = MethodTypeDesc.of(info.descriptor().returnType(), this.newReturnType);
        // TODO might want to, instead, change just the call to a generated method that handles this to avoid an extra call
        context.emit(InvokeInstruction.of(Opcode.INVOKESTATIC, context.constantPool().methodRefEntry(this.converterOwner, this.converterMethod, converterDesc)));
    }

    @Override
    public void rewriteInvokeDynamic(final MethodTransformContext context, final DirectMethodHandleDesc.Kind kind, final BootstrapInfo bootstrapInfo) {
        final MethodTransformContext.MethodInfo info = context.methodInfo();
        // descriptor = invocationType of the method handle
        // For VIRTUAL/INTERFACE_VIRTUAL, descriptor includes the receiver as the first param.
        // The call descriptor strips the receiver and uses the newReturnType.
        final MethodTypeDesc callDesc = switch (kind) {
            case VIRTUAL, INTERFACE_VIRTUAL -> info.descriptor().dropParameterTypes(0, 1).changeReturnType(this.newReturnType);
            default -> info.descriptor().changeReturnType(this.newReturnType);
        };
        final MethodTypeDesc converterDesc = MethodTypeDesc.of(info.descriptor().returnType(), this.newReturnType);

        // Generate a bridge method with same signature as original invocationType.
        // Bridge: loads all params, calls the updated method, applies converter, returns.
        final String bridgeName = context.bridges().registerBridge(
            info.name() + "$" + this.converterMethod,
            info.descriptor(),
            cb -> {
                // Load all parameters (using correct slots for wide types)
                int slot = 0;
                for (int i = 0; i < info.descriptor().parameterCount(); i++) {
                    final TypeKind typeKind = TypeKind.fromDescriptor(info.descriptor().parameterType(i).descriptorString());
                    cb.loadLocal(typeKind, slot);
                    slot += typeKind.slotSize();
                }
                // Invoke the updated method
                switch (kind) {
                    case VIRTUAL -> cb.invokevirtual(info.owner(), info.name(), callDesc);
                    case INTERFACE_VIRTUAL -> cb.invokeinterface(info.owner(), info.name(), callDesc);
                    default -> cb.invokestatic(info.owner(), info.name(), callDesc);
                }
                // Apply the converter
                cb.invokestatic(this.converterOwner, this.converterMethod, converterDesc);
                cb.areturn();
            }
        );

        // Redirect the invokedynamic to the bridge; arg[2] (instantiated type) stays the same
        // because the bridge preserves the original return type.
        final ConstantDesc[] newArgs = bootstrapInfo.args().toArray(new ConstantDesc[0]);
        newArgs[ClassFiles.BOOTSTRAP_HANDLE_IDX] = MethodHandleDesc.ofMethod(
            DirectMethodHandleDesc.Kind.STATIC, context.currentClass(), bridgeName, info.descriptor());
        context.emit(InvokeDynamicInstruction.of(context.constantPool().invokeDynamicEntry(bootstrapInfo.create(newArgs))));
    }
}

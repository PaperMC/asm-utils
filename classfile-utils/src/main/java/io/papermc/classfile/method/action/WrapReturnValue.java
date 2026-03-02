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
import static io.papermc.classfile.ClassFiles.emitInvoke;

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
        if (!(descriptorPredicate instanceof MethodDescriptorPredicate.HasReturn)) {
            return Optional.of("You must use a return descriptor predicate on " + descriptorPredicate);
        }
        if (namePredicate instanceof MethodNamePredicate.Constructor) {
            return Optional.of("Cannot wrap return value of constructor");
        }
        return Optional.empty();
    }

    @Override
    public void rewriteInvoke(final MethodTransformContext context, final Opcode opcode) {
        final MethodTransformContext.MethodInfo info = context.methodInfo();
        final MethodTypeDesc callDesc = info.descriptor().changeReturnType(this.newReturnType);
        final MethodTypeDesc staticReplacement = adjustForStatic(opcode, info.owner(), info.descriptor());
        // descriptor for converting the new return type to the old type
        final MethodTypeDesc converterDesc = MethodTypeDesc.of(info.descriptor().returnType(), this.newReturnType);

        final String bridgeName = context.bridges().registerBridge(
            info.owner(),
            info.name(), // don't need to handle ctor names, not allowed
            staticReplacement,
            ParameterGeneration.standard(),
            cb -> {
                // Invoke the updated method
                emitInvoke(cb, opcode, info, callDesc, false);
                // Apply the converter
                cb.invokestatic(this.converterOwner, this.converterMethod, converterDesc);
                cb.areturn();
            }
        );

        context.emitToBridgeMethod(bridgeName, staticReplacement);
    }

    @Override
    public void rewriteInvokeDynamic(final MethodTransformContext context, final DirectMethodHandleDesc.Kind kind, final BootstrapInfo bootstrapInfo) {
        final MethodTransformContext.MethodInfo info = context.methodInfo();
        final MethodTypeDesc callDesc = info.descriptor().changeReturnType(this.newReturnType);
        final MethodTypeDesc staticReplacement = adjustForStatic(kind, info.owner(), info.descriptor());
        // descriptor for converting the new return type to the old type
        final MethodTypeDesc converterDesc = MethodTypeDesc.of(info.descriptor().returnType(), this.newReturnType);

        // Generate a bridge method with same signature as original invocationType.
        // Bridge: loads all params, calls the updated method, applies converter, returns.
        final String bridgeName = context.bridges().registerBridge(
            info.owner(),
            info.name(), // don't need to handle ctor names, not allowed
            staticReplacement,
            ParameterGeneration.standard(),
            cb -> {
                // Invoke the updated method
                emitInvoke(cb, kind, info, callDesc, false);
                // Apply the converter
                cb.invokestatic(this.converterOwner(), this.converterMethod(), converterDesc);
            }
        );

        // Redirect the invokedynamic to the bridge; arg[2] (instantiated type) stays the same
        // because the bridge preserves the original return type.
        final ConstantDesc[] newArgs = bootstrapInfo.args().toArray(new ConstantDesc[0]);
        newArgs[BOOTSTRAP_HANDLE_IDX] = context.createBridgeHandle(bridgeName, staticReplacement);
        context.emit(InvokeDynamicInstruction.of(context.constantPool().invokeDynamicEntry(bootstrapInfo.create(newArgs))));
    }
}

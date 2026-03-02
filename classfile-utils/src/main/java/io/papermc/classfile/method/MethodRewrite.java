package io.papermc.classfile.method;

import io.papermc.classfile.method.action.MethodRewriteAction;
import io.papermc.classfile.method.transform.ConstructorAwareCodeTransform;
import io.papermc.classfile.method.transform.MethodTransformContext;
import io.papermc.classfile.method.transform.SimpleMethodBodyTransform;
import io.papermc.classfile.method.transform.TrackingConsumer;
import io.papermc.classfile.transform.TransformContext;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record MethodRewrite(ClassDesc owner, MethodNamePredicate methodName, MethodDescriptorPredicate descriptor, MethodRewriteAction action) {

    public MethodRewrite {
        action.isValidFor(methodName, descriptor).ifPresent(IllegalArgumentException::new);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean doesMatch(final String name, final MethodTypeDesc descriptor) {
        return this.methodName.test(name) && this.descriptor.test(descriptor);
    }

    public boolean transformInvoke(final MethodTransformContext context, final Opcode opcode) {
        // owner validated by caller
        if (!this.doesMatch(context.methodInfo().name(), context.methodInfo().descriptor())) {
            return false;
        }
        try (TrackingConsumer<CodeElement>.Instance _ = context.prime()) {
            this.action.rewriteInvoke(context, opcode);
        }
        return true;
    }

    public boolean transformInvokeDynamic(
        final MethodTransformContext context,
        final DirectMethodHandleDesc bootstrapMethod,
        final DirectMethodHandleDesc methodHandle,
        final List<ConstantDesc> args,
        final InvokeDynamicInstruction invokeDynamic
    ) {
        // owner validated by caller

        // For VIRTUAL/INTERFACE_VIRTUAL, the descriptor includes the receiver as this first param.
        // we need to remove that so that the descriptor matches a non-dynamic invocation, which is what our API expects for matching
        final MethodTypeDesc descriptorForMatching = switch (methodHandle.kind()) {
            case VIRTUAL, INTERFACE_VIRTUAL -> methodHandle.invocationType().dropParameterTypes(0, 1);
            default -> methodHandle.invocationType();
        };
        if (!this.doesMatch(context.methodInfo().name(), descriptorForMatching)) {
            return false;
        }
        final MethodRewriteAction.BootstrapInfo info = new MethodRewriteAction.BootstrapInfo(bootstrapMethod, invokeDynamic.name().stringValue(), invokeDynamic.typeSymbol(), args);
        try (TrackingConsumer<CodeElement>.Instance _ = context.prime()) {
            this.action.rewriteInvokeDynamic(context, methodHandle.kind(), info);
        }
        return true;
    }

    public static MethodTransform createTransform(final MethodRewriteIndex index, final TransformContext context) {
        final SimpleMethodBodyTransform basicTransform = new SimpleMethodBodyTransform(index, context);
        final boolean constructorRewrites = index.hasConstructorRewrites();
        if (!constructorRewrites) {
            return MethodTransform.transformingCode(basicTransform);
        }
        return MethodTransform.transformingCode(CodeTransform.ofStateful(() -> {
            return new ConstructorAwareCodeTransform(index, basicTransform, context);
        }));
    }

}

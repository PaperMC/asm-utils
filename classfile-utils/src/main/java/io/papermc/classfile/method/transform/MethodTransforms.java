package io.papermc.classfile.method.transform;

import io.papermc.classfile.method.MethodRewrite;
import io.papermc.classfile.transform.TransformContext;
import java.lang.classfile.CodeElement;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

import static io.papermc.classfile.ClassFiles.LAMBDA_METAFACTORY;

public final class MethodTransforms {

    private MethodTransforms() {
    }

    static void writeFromCandidates(final List<MethodRewrite> candidates, final ConstantPoolBuilder poolBuilder, final CodeElement element, final BoundRewrite boundRewrite, final Consumer<CodeElement> emitter) {
        writeFromCandidates(candidates, poolBuilder, element, boundRewrite, emitter, emitter);
    }

    static void writeFromCandidates(final List<MethodRewrite> candidates, final ConstantPoolBuilder poolBuilder, final CodeElement element, final BoundRewrite boundRewrite, final Consumer<CodeElement> rewriteEmitter, final Consumer<CodeElement> originalEmitter) {
        boolean written = false;
        for (final MethodRewrite candidate : candidates) {
            written = boundRewrite.tryWrite(rewriteEmitter, poolBuilder, candidate);
            if (written) {
                break;
            }
        }
        if (!written) {
            originalEmitter.accept(element);
        }
    }

    static @Nullable BoundRewrite setupRewrite(final CodeElement element, final TransformContext context) {
        final ClassDesc owner;
        final String methodName;
        final MethodTypeDesc descriptor;
        final Writer rewriter;
        if (element instanceof final InvokeInstruction invoke) {
            owner = invoke.owner().asSymbol();
            methodName = invoke.name().stringValue();
            descriptor = invoke.typeSymbol();
            rewriter = (methodContext, rewrite) -> rewrite.transformInvoke(methodContext, invoke.opcode());
        } else if (element instanceof final InvokeDynamicInstruction invokeDynamic) {
            final DirectMethodHandleDesc bootstrapMethod = invokeDynamic.bootstrapMethod();
            final List<ConstantDesc> args = invokeDynamic.bootstrapArgs();
            if (!bootstrapMethod.owner().equals(LAMBDA_METAFACTORY) || args.size() < 2) {
                // only looking for lambda metafactory calls
                return null;
            }
            if (!(args.get(1) instanceof final DirectMethodHandleDesc methodHandle)) {
                return null;
            }
            owner = methodHandle.owner();
            methodName = methodHandle.methodName();
            // for VIRTUAL, VIRTUAL_INTERFACE, this descriptor has the receiver type as the first param.
            // we remove it later just for purposes of descriptor matching, method actions are expected to handle it accordingly
            descriptor = methodHandle.invocationType();
            rewriter = (methodContext, rewrite) -> rewrite.transformInvokeDynamic(methodContext, bootstrapMethod, methodHandle, args, invokeDynamic);
        } else {
            return null;
        }
        final MethodTransformContext.MethodInfo info = new MethodTransformContext.MethodInfo(owner, methodName, descriptor);
        return new BoundRewrite(rewriter, info, context);
    }

    record BoundRewrite(Writer writer, MethodTransformContext.MethodInfo methodInfo, TransformContext context) {

        public boolean tryWrite(final Consumer<CodeElement> emit, final ConstantPoolBuilder poolBuilder, final MethodRewrite methodRewrite) {
            final TrackingConsumer<CodeElement> checkedEmit = new TrackingConsumer<>(emit);
            final MethodTransformContext methodContext = MethodTransformContext.create(this.context, poolBuilder, this.methodInfo, checkedEmit, methodRewrite);
            return this.writer.write(methodContext, methodRewrite);
        }
    }

    @FunctionalInterface
    interface Writer {
        boolean write(MethodTransformContext context, MethodRewrite rewrite);
    }

}

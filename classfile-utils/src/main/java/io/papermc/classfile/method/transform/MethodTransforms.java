package io.papermc.classfile.method.transform;

import io.papermc.classfile.method.MethodRewrite;
import java.lang.classfile.CodeElement;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
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

    static @Nullable BoundRewrite setupRewrite(final CodeElement element) {
        final ClassDesc owner;
        final String methodName;
        final Writer rewriter;
        if (element instanceof final InvokeInstruction invoke) {
            owner = invoke.owner().asSymbol();
            methodName = invoke.name().stringValue();
            rewriter = (emit, poolBuilder, rewrite) -> rewrite.transformInvoke(emit, poolBuilder, owner, methodName, invoke);
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
            rewriter = (emit, poolBuilder, rewrite) -> rewrite.transformInvokeDynamic(emit, poolBuilder, bootstrapMethod, methodHandle, args, invokeDynamic);
        } else {
            return null;
        }
        return new BoundRewrite(rewriter, owner, methodName);
    }

    record BoundRewrite(Writer writer, ClassDesc owner, String methodName) {

        public boolean tryWrite(final Consumer<CodeElement> emit, final ConstantPoolBuilder poolBuilder, final MethodRewrite methodRewrite) {
            return this.writer.write(emit, poolBuilder, methodRewrite);
        }
    }

    @FunctionalInterface
    interface Writer {
        boolean write(Consumer<CodeElement> emit, ConstantPoolBuilder poolBuilder, MethodRewrite rewrite);
    }
}

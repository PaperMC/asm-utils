package io.papermc.classfile.method.transform;

import io.papermc.classfile.method.MethodRewrite;
import io.papermc.classfile.method.MethodRewriteIndex;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;
import java.util.List;

// won't work if we are rewriting any constructors that aren't lambdas

/**
 * This is a transform for rewriting all non-INVOKESPECIAL instructions.
 * Method constructors that aren't lambdas require iterating over the full
 * method body to remove earlier instructions. Use {@link ConstructorAwareCodeTransform}
 * for that.
 */
public class SimpleMethodBodyTransform implements CodeTransform {

    private final MethodRewriteIndex index;

    public SimpleMethodBodyTransform(final MethodRewriteIndex index) {
        this.index = index;
    }

    @Override
    public void accept(final CodeBuilder builder, final CodeElement element) {
        final MethodTransforms.BoundRewrite boundRewrite = MethodTransforms.setupRewrite(element);
        if (boundRewrite == null) {
            builder.with(element);
            return;
        }
        final List<MethodRewrite> candidates = this.index.candidates(boundRewrite.owner(), boundRewrite.methodName());
        final boolean checkInvokeSpecial = element instanceof final InvokeInstruction invoke && invoke.opcode() == Opcode.INVOKESPECIAL;
        MethodTransforms.writeFromCandidates(
            candidates,
            builder.constantPool(),
            element,
            boundRewrite,
            el -> {
                // guard against making INVOKESPECIAL changes here
                if (checkInvokeSpecial) {
                    throw new UnsupportedOperationException("Cannot make INVOKESPECIAL instruction changes here");
                }
                builder.with(el);
            },
            builder::with
        );

    }
}

package io.papermc.classfile.method.action;

import io.papermc.classfile.ClassFiles;
import io.papermc.classfile.method.MethodDescriptorPredicate;
import io.papermc.classfile.method.MethodNamePredicate;
import io.papermc.classfile.method.transform.MethodTransformContext;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

import static io.papermc.classfile.ClassFiles.CONSTRUCTOR_METHOD_NAME;
import static io.papermc.classfile.ClassFiles.adjustForStatic;
import static io.papermc.classfile.ClassFiles.constructorMethodName;

/**
 * A record that enables the rewriting of method invocation instructions by redirecting
 * the method call to a static method on another owner.
 * This record implements the {@link MethodRewriteAction} interface, providing functionality
 * for rewriting both standard invoke instructions and dynamic invocations.
 *
 * @param newOwner The target class (owner) for the rewritten method call.
 * @param staticMethodName The method name to be used if this action represents a constructor call.
 *                              Otherwise, the method name will be {@code}create{type_name}"{@code}
 */
public record DirectStaticCall(ClassDesc newOwner, @Nullable String staticMethodName) implements MethodRewriteAction {


    public DirectStaticCall(final ClassDesc newOwner) {
        this(newOwner, null);
    }

    @Override
    public Optional<String> isValidFor(final MethodNamePredicate namePredicate, final MethodDescriptorPredicate descriptorPredicate) {
        return Optional.empty();
    }

    private String constructorStaticMethodName(final ClassDesc owner) {
        return Objects.requireNonNullElseGet(this.staticMethodName, () -> constructorMethodName(owner));
    }

    private String staticMethodName(final String originalName) {
        if (this.staticMethodName != null) {
            return this.staticMethodName;
        }
        return originalName;
    }

    @Override
    public void rewriteInvoke(final MethodTransformContext context, final Opcode opcode) {
        final MethodTypeDesc descriptor = context.methodInfo().descriptor();
        final ClassDesc owner = context.methodInfo().owner();
        final String name = context.methodInfo().name();
        final MethodTypeDesc newDescriptor = adjustForStatic(opcode, owner, descriptor);
        final String newMethodName;
        if (opcode == Opcode.INVOKESPECIAL) {
            if (CONSTRUCTOR_METHOD_NAME.equals(name)) {
                newMethodName = this.constructorStaticMethodName(owner);
            } else {
                throw new UnsupportedOperationException("Unhandled static rewrite: " + opcode + " " + owner + " " + name + " " + descriptor);
            }
        } else {
            newMethodName = this.staticMethodName(name);
        }
        context.emit(InvokeInstruction.of(Opcode.INVOKESTATIC, context.constantPool().methodRefEntry(this.newOwner(), newMethodName, newDescriptor)));
    }

    @Override
    public void rewriteInvokeDynamic(final MethodTransformContext context, final DirectMethodHandleDesc.Kind kind, final BootstrapInfo bootstrapInfo) {
        final MethodTypeDesc descriptor = context.methodInfo().descriptor();
        final ClassDesc owner = context.methodInfo().owner();
        final String name = context.methodInfo().name();
        final MethodTypeDesc newDescriptor = adjustForStatic(kind, owner, descriptor);
        final ConstantDesc[] newBootstrapArgs = bootstrapInfo.args().toArray(new ConstantDesc[0]);
        if (kind == DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL || kind == DirectMethodHandleDesc.Kind.VIRTUAL) {
            newBootstrapArgs[ClassFiles.BOOTSTRAP_HANDLE_IDX] = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, this.newOwner(), this.staticMethodName(name), newDescriptor);
        } else if (kind == DirectMethodHandleDesc.Kind.CONSTRUCTOR) {
            if (ClassFiles.CONSTRUCTOR_METHOD_NAME.equals(name)) {
                newBootstrapArgs[ClassFiles.BOOTSTRAP_HANDLE_IDX] = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, this.newOwner(), this.constructorStaticMethodName(owner), newDescriptor);
                // TODO not really needed on **every** rewrite, just the fuzzy param ones, but it doesn't seem to break anything since it will always be the same
                newBootstrapArgs[ClassFiles.DYNAMIC_TYPE_IDX] = newDescriptor;
            } else {
                throw new UnsupportedOperationException("Unhandled static rewrite: " + kind + " " + owner + " " + name + " " + descriptor);
            }
        } else if (kind != DirectMethodHandleDesc.Kind.STATIC && kind != DirectMethodHandleDesc.Kind.INTERFACE_STATIC) {
            throw new UnsupportedOperationException("Unhandled static rewrite: " + kind + " " + owner + " " + name + " " + descriptor);
        } else {
            // is a static method
            newBootstrapArgs[ClassFiles.BOOTSTRAP_HANDLE_IDX] = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, this.newOwner(), this.staticMethodName(name), newDescriptor);
        }
        context.emit(InvokeDynamicInstruction.of(context.constantPool().invokeDynamicEntry(bootstrapInfo.create(newBootstrapArgs))));
    }
}

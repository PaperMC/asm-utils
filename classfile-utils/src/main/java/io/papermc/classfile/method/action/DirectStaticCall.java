package io.papermc.classfile.method.action;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.Opcode;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * A record that enables the rewriting of method invocation instructions by redirecting
 * the method call to a static method on another owner.
 * This record implements the {@link MethodRewriteAction} interface, providing functionality
 * for rewriting both standard invoke instructions and dynamic invocations.
 *
 * @param newOwner The target class (owner) for the rewritten method call.
 * @param constructorMethodName The method name to be used if this action represents a constructor call.
 *                              Otherwise, the method name will be {@code}create{type_name}"{@code}
 */
public record DirectStaticCall(ClassDesc newOwner, @Nullable String constructorMethodName) implements MethodRewriteAction {

    private static final String DEFAULT_CTOR_METHOD_PREFIX = "create";

    public DirectStaticCall(final ClassDesc newOwner) {
        this(newOwner, null);
    }

    private String constructorStaticMethodName(final ClassDesc owner) {
        if (this.constructorMethodName != null) {
            return this.constructorMethodName;
        }
        // strip preceding "L" and trailing ";""
        final String ownerName = owner.descriptorString().substring(1, owner.descriptorString().length() - 1);
        return DEFAULT_CTOR_METHOD_PREFIX + ownerName.substring(ownerName.lastIndexOf('/') + 1);
    }

    @Override
    public void rewriteInvoke(final Consumer<CodeElement> emit, final ConstantPoolBuilder poolBuilder, final Opcode opcode, final ClassDesc owner, final String name, final MethodTypeDesc descriptor) {
        MethodTypeDesc newDescriptor = descriptor;
        if (opcode == Opcode.INVOKEVIRTUAL || opcode == Opcode.INVOKEINTERFACE) {
            newDescriptor = descriptor.insertParameterTypes(0, owner);
        } else if (opcode == Opcode.INVOKESPECIAL) {
            if (CONSTRUCTOR_METHOD_NAME.equals(name)) {
                newDescriptor = newDescriptor.changeReturnType(owner);
                emit.accept(InvokeInstruction.of(Opcode.INVOKESTATIC, poolBuilder.methodRefEntry(this.newOwner(), this.constructorStaticMethodName(owner), newDescriptor)));
                // builder.invokestatic(this.newOwner(), this.constructorStaticMethodName(owner), newDescriptor, false);
                return;
            } else {
                throw new UnsupportedOperationException("Unhandled static rewrite: " + opcode + " " + owner + " " + name + " " + descriptor);
            }
        } else if (opcode != Opcode.INVOKESTATIC) {
            throw new UnsupportedOperationException("Unhandled static rewrite: " + opcode + " " + owner + " " + name + " " + descriptor);
        }
        emit.accept(InvokeInstruction.of(Opcode.INVOKESTATIC, poolBuilder.methodRefEntry(this.newOwner(), name, newDescriptor)));
        // builder.invokestatic(this.newOwner(), name, newDescriptor, false);
    }

    @Override
    public void rewriteInvokeDynamic(final CodeBuilder builder,
                                     final DirectMethodHandleDesc.Kind kind,
                                     final ClassDesc owner,
                                     final String name,
                                     final MethodTypeDesc descriptor,
                                     final BootstrapInfo bootstrapInfo) {
        MethodTypeDesc newDescriptor = descriptor;
        final ConstantDesc[] newBootstrapArgs = bootstrapInfo.args().toArray(new ConstantDesc[0]);
        if (kind == DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL || kind == DirectMethodHandleDesc.Kind.VIRTUAL) {
            // TODO make sure we don't need this. The descriptor already seems to always have the "instance" as the first param if it exists
            // newDescriptor = descriptor.insertParameterTypes(0, owner);
            newBootstrapArgs[BOOTSTRAP_HANDLE_IDX] = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, this.newOwner(), name, newDescriptor);
        } else if (kind == DirectMethodHandleDesc.Kind.SPECIAL || kind == DirectMethodHandleDesc.Kind.INTERFACE_SPECIAL || kind == DirectMethodHandleDesc.Kind.CONSTRUCTOR) {
            if (CONSTRUCTOR_METHOD_NAME.equals(name)) {
                newDescriptor = newDescriptor.changeReturnType(owner);
                newBootstrapArgs[BOOTSTRAP_HANDLE_IDX] = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC,
                    this.newOwner(),
                    this.constructorStaticMethodName(owner),
                    newDescriptor
                );
                // TODO not really needed on **every** rewrite, just the fuzzy param ones, but it doesn't seem to break anything since it will always be the same
                newBootstrapArgs[DYNAMIC_TYPE_IDX] = newDescriptor;
            } else {
                throw new UnsupportedOperationException("Unhandled static rewrite: " + kind + " " + owner + " " + name + " " + descriptor);
            }
        } else if (kind != DirectMethodHandleDesc.Kind.STATIC && kind != DirectMethodHandleDesc.Kind.INTERFACE_STATIC) {
            throw new UnsupportedOperationException("Unhandled static rewrite: " + kind + " " + owner + " " + name + " " + descriptor);
        } else {
            // is a static method
            newBootstrapArgs[BOOTSTRAP_HANDLE_IDX] = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, this.newOwner(), name, newDescriptor);
        }
        builder.invokedynamic(bootstrapInfo.create(newBootstrapArgs));
    }
}

package io.papermc.classfile.method.transform;

import io.papermc.classfile.method.MethodRewrite;
import io.papermc.classfile.transform.TransformContext;
import java.lang.classfile.CodeElement;
import java.lang.classfile.Opcode;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

public interface MethodTransformContext extends TransformContext {

    static MethodTransformContext create(
        final TransformContext context,
        final ConstantPoolBuilder constantPool,
        final MethodInfo methodInfo,
        final Consumer<CodeElement> emit,
        final MethodRewrite currentRewrite
    ) {
        return new MethodTransformContextImpl(context.currentClass(), context.bridges(), constantPool, methodInfo, new TrackingConsumer<>(emit), currentRewrite);
    }

    default void emitChangedDescriptor(final Opcode opcode, final MethodTypeDesc newDescriptor) {
        final MemberRefEntry ref = opcode == Opcode.INVOKEINTERFACE
            ? this.constantPool().interfaceMethodRefEntry(this.methodInfo().owner(), this.methodInfo().name(), newDescriptor)
            : this.constantPool().methodRefEntry(this.methodInfo().owner(), this.methodInfo().name(), newDescriptor);
        this.emit(InvokeInstruction.of(opcode, ref));
    }

    default void emitToBridgeMethod(final String name, final MethodTypeDesc descriptor) {
        this.emit(InvokeInstruction.of(
            Opcode.INVOKESTATIC, this.constantPool().methodRefEntry(this.currentClass(), name, descriptor)
        ));
    }

    default DirectMethodHandleDesc createBridgeHandle(final String name, final MethodTypeDesc descriptor) {
        return MethodHandleDesc.ofMethod(
            DirectMethodHandleDesc.Kind.STATIC,
            this.currentClass(),
            name,
            descriptor
        );
    }

    /**
     * Emits a given {@link CodeElement} for inclusion in the method's bytecode.
     *
     * @param element the {@link CodeElement} to be emitted. This represents a single
     *                instruction or other component to be added to the method body.
     */
    void emit(CodeElement element);

    /**
     * Gets the current method rewrite being applied.
     *
     * @return the current method rewrite
     */
    MethodRewrite currentRewrite();

    /**
     * Provides access to the constant pool builder associated with the current method transformation.
     * The constant pool builder enables adding or resolving constant pool entries needed during
     * bytecode generation or transformation.
     *
     * @return the {@link ConstantPoolBuilder} for managing constant pool entries.
     */
    ConstantPoolBuilder constantPool();

    /**
     * Retrieves information about a specific method, combining metadata associated
     * with method invocation instructions such as INVOKE and INVOKEDYNAMIC.
     *
     * @return a {@link MethodInfo} record representing the method's owner,
     *         name, and descriptor, providing essential details for method matching
     *         during bytecode transformation or analysis.
     */
    MethodInfo methodInfo();

    /**
     * This is just for method matching, combining method information from both
     * INVOKE* and INVOKEDYNAMIC instructions.
     */
    record MethodInfo(ClassDesc owner, String name, MethodTypeDesc descriptor, boolean isInterface) {
    }

    /**
     * Call this in a {@code try-with-resources} block to make sure the {@link #emit(CodeElement)}
     * is actually called.
     *
     * @return an {@link AutoCloseable} for a {@code try-with-resources} block
     */
    TrackingConsumer<CodeElement>.Instance prime();
}

package io.papermc.classfile.method.transform;

import io.papermc.classfile.ClassFiles;
import java.lang.classfile.Opcode;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.MethodRefEntry;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestConstructorAwareCodeTransform {

    @Test
    void nonInvokeInstructionReturnsFalse() {
        final NewObjectInstruction element = NewObjectInstruction.of(ConstantPoolBuilder.of().classEntry(ClassDesc.of("java.lang.Object")));
        assertThat(ConstructorAwareCodeTransform.isConstructor(element)).isFalse();
    }

    @Test
    void invokespecialInitReturnsTrue() {
        final ConstantPoolBuilder pool = ConstantPoolBuilder.of();
        final MethodRefEntry ref = pool.methodRefEntry(ClassDesc.of("java.lang.Object"), ClassFiles.CONSTRUCTOR_METHOD_NAME, MethodTypeDesc.of(ConstantDescs.CD_void));
        final InvokeInstruction invoke = InvokeInstruction.of(Opcode.INVOKESPECIAL, ref);
        assertThat(ConstructorAwareCodeTransform.isConstructor(invoke)).isTrue();
    }

    @Test
    void invokespecialNonInitReturnsFalse() {
        final ConstantPoolBuilder pool = ConstantPoolBuilder.of();
        final MethodRefEntry ref = pool.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.of(ClassDesc.of("java.lang.String")));
        final InvokeInstruction invoke = InvokeInstruction.of(Opcode.INVOKESPECIAL, ref);
        assertThat(ConstructorAwareCodeTransform.isConstructor(invoke)).isFalse();
    }

    @Test
    void invokevirtualInitReturnsFalse() {
        final ConstantPoolBuilder pool = ConstantPoolBuilder.of();
        final MethodRefEntry ref = pool.methodRefEntry(ClassDesc.of("java.lang.Object"), ClassFiles.CONSTRUCTOR_METHOD_NAME, MethodTypeDesc.of(ConstantDescs.CD_void));
        final InvokeInstruction invoke = InvokeInstruction.of(Opcode.INVOKEVIRTUAL, ref);
        assertThat(ConstructorAwareCodeTransform.isConstructor(invoke)).isFalse();
    }
}

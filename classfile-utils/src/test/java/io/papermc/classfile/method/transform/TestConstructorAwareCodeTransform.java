package io.papermc.classfile.method.transform;

import io.papermc.classfile.ClassFiles;
import java.lang.classfile.CodeElement;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestConstructorAwareCodeTransform {

    @Test
    void nonInvokeInstructionReturnsFalse() {
        final CodeElement element = mock(NewObjectInstruction.class);
        assertThat(ConstructorAwareCodeTransform.isConstructor(element)).isFalse();
    }

    @Test
    void invokespecialInitReturnsTrue() {
        final InvokeInstruction invoke = mock(InvokeInstruction.class, RETURNS_DEEP_STUBS);
        when(invoke.opcode()).thenReturn(Opcode.INVOKESPECIAL);
        when(invoke.method().name().equalsString(ClassFiles.CONSTRUCTOR_METHOD_NAME)).thenReturn(true);
        assertThat(ConstructorAwareCodeTransform.isConstructor(invoke)).isTrue();
    }

    @Test
    void invokespecialNonInitReturnsFalse() {
        final InvokeInstruction invoke = mock(InvokeInstruction.class, RETURNS_DEEP_STUBS);
        when(invoke.opcode()).thenReturn(Opcode.INVOKESPECIAL);
        when(invoke.method().name().equalsString(ClassFiles.CONSTRUCTOR_METHOD_NAME)).thenReturn(false);
        assertThat(ConstructorAwareCodeTransform.isConstructor(invoke)).isFalse();
    }

    @Test
    void invokevirtualInitReturnsFalse() {
        final InvokeInstruction invoke = mock(InvokeInstruction.class, RETURNS_DEEP_STUBS);
        when(invoke.opcode()).thenReturn(Opcode.INVOKEVIRTUAL);
        assertThat(ConstructorAwareCodeTransform.isConstructor(invoke)).isFalse();
    }
}

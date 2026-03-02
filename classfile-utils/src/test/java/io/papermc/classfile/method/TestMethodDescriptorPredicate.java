package io.papermc.classfile.method;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestMethodDescriptorPredicate {

    static final ClassDesc STRING = ClassDesc.of("java.lang.String");
    static final ClassDesc INTEGER = ClassDesc.of("java.lang.Integer");
    static final ClassDesc OBJECT = ClassDesc.of("java.lang.Object");

    @Test
    void hasReturnMatchesWhenReturnTypeMatches() {
        final MethodDescriptorPredicate predicate = MethodDescriptorPredicate.hasReturn(STRING);
        final MethodTypeDesc desc = MethodTypeDesc.of(STRING, OBJECT);
        assertThat(predicate.test(desc)).isTrue();
    }

    @Test
    void hasReturnNoMatchWhenReturnTypeDiffers() {
        final MethodDescriptorPredicate predicate = MethodDescriptorPredicate.hasReturn(STRING);
        final MethodTypeDesc desc = MethodTypeDesc.of(OBJECT, STRING);
        assertThat(predicate.test(desc)).isFalse();
    }

    @Test
    void hasReturnNoMatchVoidReturn() {
        final MethodDescriptorPredicate predicate = MethodDescriptorPredicate.hasReturn(STRING);
        final MethodTypeDesc desc = MethodTypeDesc.of(ConstantDescs.CD_void, STRING);
        assertThat(predicate.test(desc)).isFalse();
    }

    @Test
    void hasReturnTargetTypeIsReturnType() {
        final MethodDescriptorPredicate predicate = MethodDescriptorPredicate.hasReturn(STRING);
        assertThat(predicate.targetType()).isEqualTo(STRING);
    }

    @Test
    void hasParameterMatchesWhenParamPresent() {
        final MethodDescriptorPredicate predicate = MethodDescriptorPredicate.hasParameter(STRING);
        final MethodTypeDesc desc = MethodTypeDesc.of(ConstantDescs.CD_void, INTEGER, STRING, OBJECT);
        assertThat(predicate.test(desc)).isTrue();
    }

    @Test
    void hasParameterMatchesSingleParam() {
        final MethodDescriptorPredicate predicate = MethodDescriptorPredicate.hasParameter(STRING);
        final MethodTypeDesc desc = MethodTypeDesc.of(ConstantDescs.CD_void, STRING);
        assertThat(predicate.test(desc)).isTrue();
    }

    @Test
    void hasParameterNoMatchWhenParamAbsent() {
        final MethodDescriptorPredicate predicate = MethodDescriptorPredicate.hasParameter(STRING);
        final MethodTypeDesc desc = MethodTypeDesc.of(ConstantDescs.CD_void, INTEGER, OBJECT);
        assertThat(predicate.test(desc)).isFalse();
    }

    @Test
    void hasParameterNoMatchNoParams() {
        final MethodDescriptorPredicate predicate = MethodDescriptorPredicate.hasParameter(STRING);
        final MethodTypeDesc desc = MethodTypeDesc.of(ConstantDescs.CD_void);
        assertThat(predicate.test(desc)).isFalse();
    }

    @Test
    void hasParameterTargetTypeIsParamType() {
        final MethodDescriptorPredicate predicate = MethodDescriptorPredicate.hasParameter(STRING);
        assertThat(predicate.targetType()).isEqualTo(STRING);
    }
}

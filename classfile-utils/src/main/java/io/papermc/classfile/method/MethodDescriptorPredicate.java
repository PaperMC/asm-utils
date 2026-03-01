package io.papermc.classfile.method;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Predicate;

public sealed interface MethodDescriptorPredicate extends Predicate<MethodTypeDesc> {

    ClassDesc targetType();

    static MethodDescriptorPredicate hasReturn(final ClassDesc returnType) {
        return new ReturnType(returnType);
    }

    static MethodDescriptorPredicate hasParameter(final ClassDesc parameterType) {
        return new HasParameter(parameterType);
    }

    record ReturnType(ClassDesc targetType) implements MethodDescriptorPredicate {

        @Override
        public boolean test(final MethodTypeDesc methodTypeDesc) {
            return methodTypeDesc.returnType().equals(this.targetType);
        }
    }

    record HasParameter(ClassDesc targetType) implements MethodDescriptorPredicate {

        @Override
        public boolean test(final MethodTypeDesc methodTypeDesc) {
            return methodTypeDesc.parameterList().contains(this.targetType);
        }
    }
}

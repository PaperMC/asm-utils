package io.papermc.classfile;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.LambdaMetafactory;
import java.util.Set;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

public final class ClassFiles {

    public static final int BOOTSTRAP_HANDLE_IDX = 1;
    public static final int DYNAMIC_TYPE_IDX = 2;
    public static final String CONSTRUCTOR_METHOD_NAME = "<init>";
    public static final ClassDesc LAMBDA_METAFACTORY = desc(LambdaMetafactory.class);

    private ClassFiles() {
    }

    public static ClassDesc desc(final Class<?> clazz) {
        return clazz.describeConstable().orElseThrow();
    }

    public static MethodTypeDesc replaceParameters(MethodTypeDesc descriptor, final Predicate<ClassDesc> oldParam, final ClassDesc newParam) {
        for (int i = 0; i < descriptor.parameterCount(); i++) {
            if (oldParam.test(descriptor.parameterType(i))) {
                descriptor = descriptor.changeParameterType(i, newParam);
            }
        }
        return descriptor;
    }
}

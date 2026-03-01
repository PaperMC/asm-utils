package io.papermc.classfile;

import java.lang.constant.ClassDesc;
import java.lang.invoke.LambdaMetafactory;

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

    public static boolean startsWith(final CharSequence text, final char[] prefix) {
        final int len = prefix.length;
        if (text.length() < len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (text.charAt(i) != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}

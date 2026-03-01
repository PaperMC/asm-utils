package io.papermc.classfile;

import java.lang.constant.ClassDesc;

public final class ClassfileUtils {

    private ClassfileUtils() {
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

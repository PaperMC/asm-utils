package io.papermc.asm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ClassInfoProvider {
    @Nullable ClassInfo info(String className);

    static ClassInfoProvider basic() {
        return className -> {
            try {
                // Note: Will not work if the class is not already on the classpath, most likely don't want to use this
                final Class<?> clazz = Class.forName(className.replace("/", "."));
                return ClassInfo.create(
                    className,
                    clazz.isEnum(),
                    clazz.getSuperclass() == null ? null : clazz.getSuperclass().getName().replace(".", "/")
                );
            } catch (final ClassNotFoundException ex) {
                return null;
            }
        };
    }

    static ClassInfoProvider caching(
        final ClassInfoProvider backing,
        final boolean cacheMisses,
        final int cacheSize
    ) {
        return new ClassInfoProvider() {
            private static final ClassInfo NULL_INFO = ClassInfo.create("", false, null);

            private final Map<String, ClassInfo> classInfoCache = Collections.synchronizedMap(new LinkedHashMap<>(cacheSize, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(final Map.Entry<String, ClassInfo> eldest) {
                    return this.size() > cacheSize - 1;
                }
            });

            @Override
            public @Nullable ClassInfo info(final String className) {
                final ClassInfo info = this.classInfoCache.computeIfAbsent(className, cls -> {
                    final ClassInfo find = backing.info(cls);
                    return find == null && cacheMisses ? NULL_INFO : find;
                });
                return info == NULL_INFO ? null : info;
            }
        };
    }
}

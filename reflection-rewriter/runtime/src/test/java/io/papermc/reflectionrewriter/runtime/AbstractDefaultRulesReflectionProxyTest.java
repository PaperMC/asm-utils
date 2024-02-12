package io.papermc.reflectionrewriter.runtime;

import java.lang.invoke.MethodType;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractDefaultRulesReflectionProxyTest {
    @Test
    void testClassForName() {
        final AbstractDefaultRulesReflectionProxy proxy = createTestProxy();

        assertClassIsMappedTo(proxy, "java.lang.String", "java.lang.StringBuilder");
        assertClassIsMappedTo(proxy, "[Ljava.lang.String;", "[Ljava.lang.StringBuilder;");
        assertClassIsMappedTo(proxy, "[[[Ljava.lang.String;", "[[[Ljava.lang.StringBuilder;");

        // Shouldn't get mapped
        assertClassNotMapped(proxy, "java.lang.invoke.MethodType");
        assertClassNotMapped(proxy, "[Ljava.lang.invoke.MethodType;");
    }

    static void assertClassNotMapped(final AbstractDefaultRulesReflectionProxy proxy, final String name) {
        assertClassIsMappedTo(proxy, name, name);
    }

    static void assertClassIsMappedTo(final AbstractDefaultRulesReflectionProxy proxy, final String unmapped, final String mapped) {
        try {
            final Class<?> mappedType = proxy.forName(unmapped);
            final Class<?> directType = Class.forName(mapped);
            assertEquals(directType.getName(), mappedType.getName());
        } catch (final ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void testMethodTypeFromMethodDescriptorString() {
        final AbstractDefaultRulesReflectionProxy proxy = createTestProxy();
        assertDescIsMappedTo(
            proxy,
            "(Ljava/lang/invoke/MethodType;ILjava/lang/String;)Ljava/lang/String;",
            "(Ljava/lang/invoke/MethodType;ILjava/lang/StringBuilder;)Ljava/lang/StringBuilder;"
        );
        assertDescIsMappedTo(proxy, "([Ljava/lang/String;)J", "([Ljava/lang/StringBuilder;)J");

        // Shouldn't get mapped
        assertDescNotMapped(proxy, "([Ljava/lang/StringBuilder;)J");
        assertDescNotMapped(proxy, "(Ljava/lang/StringBuilder;)I");
    }

    static void assertDescNotMapped(final AbstractDefaultRulesReflectionProxy proxy, final String desc) {
        assertDescIsMappedTo(proxy, desc, desc);
    }

    static void assertDescIsMappedTo(final AbstractDefaultRulesReflectionProxy proxy, final String unmapped, final String mapped) {
        final ClassLoader loader = AbstractDefaultRulesReflectionProxyTest.class.getClassLoader();
        final MethodType mappedType = proxy.fromMethodDescriptorString(unmapped, loader);
        final MethodType directType = MethodType.fromMethodDescriptorString(mapped, loader);
        assertEquals(directType.descriptorString(), mappedType.descriptorString());
    }

    static AbstractDefaultRulesReflectionProxy createTestProxy() {
        return new TestProxy(Map.of(
            "java.lang.String", "java.lang.StringBuilder"
        ));
    }

    static class TestProxy extends AbstractDefaultRulesReflectionProxy {
        final Map<String, String> classMappings;

        TestProxy(final Map<String, String> classMappings) {
            this.classMappings = classMappings;
        }

        @Override
        protected String mapClassName(final String name) {
            return this.classMappings.getOrDefault(name, name);
        }

        @Override
        protected String mapDeclaredMethodName(final Class<?> clazz, final String name, final Class<?>... parameterTypes) {
            return name;
        }

        @Override
        protected String mapMethodName(final Class<?> clazz, final String name, final Class<?>... parameterTypes) {
            return name;
        }

        @Override
        protected String mapDeclaredFieldName(final Class<?> clazz, final String name) {
            return name;
        }

        @Override
        protected String mapFieldName(final Class<?> clazz, final String name) {
            return name;
        }
    }
}

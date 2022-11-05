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

    static void assertClassNotMapped(AbstractDefaultRulesReflectionProxy proxy, String name) {
        assertClassIsMappedTo(proxy, name, name);
    }

    static void assertClassIsMappedTo(AbstractDefaultRulesReflectionProxy proxy, String unmapped, String mapped) {
        try {
            final Class<?> mappedType = proxy.forName(unmapped);
            final Class<?> directType = Class.forName(mapped);
            assertEquals(directType.getName(), mappedType.getName());
        } catch (ClassNotFoundException ex) {
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

    static void assertDescNotMapped(AbstractDefaultRulesReflectionProxy proxy, String desc) {
        assertDescIsMappedTo(proxy, desc, desc);
    }

    static void assertDescIsMappedTo(AbstractDefaultRulesReflectionProxy proxy, String unmapped, String mapped) {
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

        TestProxy(Map<String, String> classMappings) {
            this.classMappings = classMappings;
        }

        @Override
        protected String mapClassName(String name) {
            return this.classMappings.getOrDefault(name, name);
        }

        @Override
        protected String mapDeclaredMethodName(Class<?> clazz, String name, Class<?>... parameterTypes) {
            return name;
        }

        @Override
        protected String mapMethodName(Class<?> clazz, String name, Class<?>... parameterTypes) {
            return name;
        }

        @Override
        protected String mapDeclaredFieldName(Class<?> clazz, String name) {
            return name;
        }

        @Override
        protected String mapFieldName(Class<?> clazz, String name) {
            return name;
        }
    }
}

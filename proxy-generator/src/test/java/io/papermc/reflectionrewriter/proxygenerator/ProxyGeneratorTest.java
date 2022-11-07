package io.papermc.reflectionrewriter.proxygenerator;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProxyGeneratorTest {
    @Test
    void test() throws ReflectiveOperationException {
        final byte[] generatedBytes = ProxyGenerator.generateProxy(
            TestProxyImpl.class,
            "io/papermc/reflectionrewriter/proxygenerator/GeneratedProxy"
        );
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final Class<?> generatedClass = lookup.defineClass(generatedBytes);
        final Method init = generatedClass.getDeclaredMethod("init", TestProxyImpl.class);
        final TestProxyImpl instance = new TestProxyImpl();
        init.invoke(null, instance);

        assertEquals(instance.defaultMethodTest(), generatedClass.getDeclaredMethod("defaultMethodTest").invoke(null));
        assertEquals(instance.zero(), (int) generatedClass.getDeclaredMethod("zero").invoke(null));
        assertEquals(instance.one(), (double) generatedClass.getDeclaredMethod("one").invoke(null));
        assertEquals(instance.plusOne(1), (int) generatedClass.getDeclaredMethod("plusOne", int.class).invoke(null, 1));
        assertEquals(instance.repeat("hello"), generatedClass.getDeclaredMethod("repeat", String.class).invoke(null, "hello"));
        assertEquals(instance.multiply(1.5D, 3), (double) generatedClass.getDeclaredMethod("multiply", double.class, int.class).invoke(null, 1.5D, 3));
        generatedClass.getDeclaredMethod("run").invoke(null);
    }

    interface TestProxy {
        default String defaultMethodTest() {
            return "default method result!";
        }

        int zero();

        int plusOne(int i);
    }

    static abstract class AbstractTestProxy implements TestProxy {
        @Override
        public int zero() {
            return 0;
        }

        @Override
        public int plusOne(final int i) {
            return i + 1;
        }
    }

    static class TestProxyImpl extends AbstractTestProxy implements Runnable {
        public double one() {
            return 1.00D;
        }

        public double multiply(final double a, final int b) {
            return a * b;
        }

        public String repeat(final String s) {
            return s + s;
        }

        @Override
        public void run() {
        }
    }
}

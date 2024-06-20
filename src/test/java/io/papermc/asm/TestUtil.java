package io.papermc.asm;

import io.papermc.asm.rules.RewriteRule;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DefaultQualifier(NonNull.class)
public final class TestUtil {
    private TestUtil() {
    }

    public static RewriteRuleVisitorFactory testingVisitorFactory(final RewriteRule rewriteRule) {
        return RewriteRuleVisitorFactory.create(Opcodes.ASM9, rewriteRule, ClassInfoProvider.basic());
    }

    public static Map<String, byte[]> inputBytes(final String className) {
        return readClassBytes(new HashMap<>(), className, n -> n + ".class");
    }

    public interface Processor<E extends Throwable> {
        byte[] process(byte[] bytes) throws E;
    }

    public record DefaultProcessor(RewriteRuleVisitorFactory factory) implements Processor<RuntimeException> {
        @Override
        public byte[] process(final byte[] bytes) {
            final ClassReader classReader = new ClassReader(bytes);
            final ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            classReader.accept(this.factory.createVisitor(classWriter), 0);
            return classWriter.toByteArray();
        }
    }

    public static void assertProcessedMatchesExpected(
        final String className,
        final RewriteRuleVisitorFactory factory
    ) {
        assertProcessedMatchesExpected_(className, new DefaultProcessor(factory));
    }

    private static boolean checkJavapDiff(final String name, final byte[] expected, final byte[] processed, final List<String> javapCommand) {
        try {
            Path tmp = Files.createTempDirectory("tmpasmutils");
            Path cls = tmp.resolve("cls.class");
            Files.write(cls, expected);
            final List<String> command = new ArrayList<>(javapCommand);
            command.add("cls");
            final Process proc = new ProcessBuilder(command)
                .directory(tmp.toFile())
                .redirectErrorStream(true)
                .start();
            final String expectedJavap = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            proc.waitFor(5, TimeUnit.SECONDS);

            tmp = Files.createTempDirectory("tmpasmutils");
            cls = tmp.resolve("cls.class");
            Files.write(cls, processed);
            final Process proc1 = new ProcessBuilder(command)
                .directory(tmp.toFile())
                .redirectErrorStream(true)
                .start();
            final String actualJavap = new String(proc1.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            proc1.waitFor(5, TimeUnit.SECONDS);

            assertEquals(expectedJavap, actualJavap, () -> "Transformed class bytes did not match expected for " + name);
        } catch (final IOException exception) {
            exception.printStackTrace();
            System.err.println("Failed to diff class bytes using javap, falling back to direct byte comparison.");
            return false;
        } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    private static <T extends Throwable> void assertProcessedMatchesExpected_(
        final String className,
        final Processor<T> processor
    ) {
        final Map<String, byte[]> input = inputBytes(className.replace(".", "/"));
        final Map<String, byte[]> processed = processClassBytes(input, processor);
        final Map<String, byte[]> expected;
        try {
            expected = expectedBytes(className.replace(".", "/"));
        } catch (final RuntimeException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                final Path expectedDir = Path.of("src/testData/resources/expected");
                for (final Map.Entry<String, byte[]> entry : processed.entrySet()) {
                    final Path outPath = expectedDir.resolve(entry.getKey() + ".class");
                    if (Files.exists(outPath)) {
                        throw new IllegalStateException();
                    }
                    try {
                        Files.createDirectories(outPath.getParent());
                        Files.write(outPath, entry.getValue());
                    } catch (final IOException ex0) {
                        throw new RuntimeException(ex0);
                    }
                }
                throw new RuntimeException("Expected data not present, wrote current processed output.");
            }
            throw e;
        }
        for (final String name : input.keySet()) {
            if (Arrays.equals(expected.get(name), processed.get(name))) {
                // Bytes equal
                return;
            } else {
                // Try to get a javap diff
                // final boolean proceed = checkJavapDiff(name, expected.get(name), processed.get(name), Arrays.asList("javap", "-c", "-p"));
                // verbose is too useful for invokedynamic debugging to omit
                checkJavapDiff(name, expected.get(name), processed.get(name), Arrays.asList("javap", "-c", "-p", "-v"));

                // If javap failed, just assert the bytes equal
                assertArrayEquals(
                    expected.get(name),
                    processed.get(name),
                    () -> "Transformed class bytes did not match expected for " + name
                );
            }
        }
    }

    @SuppressWarnings({"RedundantCast", "unchecked"})
    public static Map<String, byte[]> processClassBytes(
        final Map<String, byte[]> input,
        final Processor<?> proc
    ) {
        final Map<String, byte[]> output = new HashMap<>(input.size());
        for (final Map.Entry<String, byte[]> entry : input.entrySet()) {
            output.put(entry.getKey(), ((Processor<RuntimeException>) proc).process(entry.getValue()));
        }
        return output;
    }

    public static Map<String, byte[]> expectedBytes(final String className) {
        return readClassBytes(new HashMap<>(), className, n -> "expected/" + n + ".class");
    }

    private static Map<String, byte[]> readClassBytes(
        final Map<String, byte[]> map,
        final String className,
        final Function<String, String> classNameMapper
    ) {
        try {
            final @Nullable URL url = TestUtil.class.getClassLoader().getResource(classNameMapper.apply(className));
            if (url == null) {
                throw new FileNotFoundException(classNameMapper.apply(className));
            }
            final InputStream s = url.openStream();
            try (s) {
                final byte[] rootBytes = s.readAllBytes();
                final ClassNode node = new ClassNode(Opcodes.ASM9);
                final ClassReader classReader = new ClassReader(rootBytes);
                classReader.accept(node, ClassReader.SKIP_CODE);
                map.put(node.name, rootBytes);
                for (final InnerClassNode innerNode : node.innerClasses) {
                    if (!innerNode.outerName.equals(node.name)) {
                        continue;
                    }
                    readClassBytes(map, innerNode.name, classNameMapper);
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static void processAndExecute(
        final String className,
        final TestUtil.Processor<?> proc
    ) {
        processAndExecute(className, proc, "entry");
    }

    public static void processAndExecute(
        final String className,
        final TestUtil.Processor<?> proc,
        final String methodName
    ) {
        final Map<String, byte[]> input = TestUtil.inputBytes(className.replace(".", "/"));
        final Map<String, byte[]> processed = TestUtil.processClassBytes(input, proc);

        final var loader = new URLClassLoader(new URL[]{}, TestUtil.class.getClassLoader()) {
            @Override
            protected Class<?> findClass(final String name) throws ClassNotFoundException {
                final String slashName = name.replace(".", "/");
                final byte[] processedBytes = processed.get(slashName);
                if (processedBytes != null) {
                    return super.defineClass(name, processedBytes, 0, processedBytes.length);
                }
                return super.findClass(name);
            }
        };

        try {
            final Class<?> loaded = loader.findClass(className);
            final Method main = loaded.getDeclaredMethod(methodName);
            main.trySetAccessible();
            main.invoke(null);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}

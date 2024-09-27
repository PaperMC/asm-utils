package io.papermc.asm;

import io.papermc.asm.rules.RewriteRule;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.spi.ToolProvider;
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

    private static final ToolProvider JAVAP_PROVIDER = ToolProvider.findFirst("javap").orElseThrow(() -> new IllegalStateException("javap not found"));

    public static RewriteRuleVisitorFactory testingVisitorFactory(final RewriteRule rewriteRule) {
        return RewriteRuleVisitorFactory.create(Opcodes.ASM9, rewriteRule, ClassInfoProvider.basic());
    }

    public static Map<String, byte[]> inputBytes(final String className) {
        return readClassBytes(new HashMap<>(), className, n -> n + ".class");
    }

    public interface Processor<E extends Throwable> {
        byte[] process(byte[] bytes) throws E;
    }

    public record DefaultProcessor(RewriteRuleVisitorFactory factory, boolean copyFromClassReader) implements Processor<RuntimeException> {
        @Override
        public byte[] process(final byte[] bytes) {
            final ClassReader classReader = new ClassReader(bytes);
            final ClassWriter classWriter;
            if (this.copyFromClassReader()) {
                classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            } else {
                classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            }
            classReader.accept(this.factory.createVisitor(classWriter), 0);
            return classWriter.toByteArray();
        }
    }

    public static void assertProcessedMatchesExpected(
        final String className,
        final RewriteRuleVisitorFactory factory,
        final boolean copyFromClassReader
    ) {
        assertProcessedMatchesExpected_(className, new DefaultProcessor(factory, copyFromClassReader));
    }

    private static boolean checkJavapDiff(final String name, final byte[] expected, final byte[] processed, final List<String> javapArgs) {
        final String[] command = new String[javapArgs.size() + 1];
        for (int i = 0; i < javapArgs.size(); i++) {
            command[i] = javapArgs.get(i);
        }
        try {
            Path tmp = Files.createTempDirectory("tmpasmutils");
            Path cls = tmp.resolve("cls.class");
            Files.write(cls, expected);
            command[javapArgs.size()] = cls.toAbsolutePath().toString();

            final StringWriter expectedStringWriter = new StringWriter();
            final PrintWriter expectedWriter = new PrintWriter(expectedStringWriter);
            JAVAP_PROVIDER.run(expectedWriter, expectedWriter, command);
            final String expectedJavap = expectedStringWriter.toString();

            tmp = Files.createTempDirectory("tmpasmutils");
            cls = tmp.resolve("cls.class");
            Files.write(cls, processed);
            command[javapArgs.size()] = cls.toAbsolutePath().toString();
            final StringWriter actualStringWriter = new StringWriter();
            final PrintWriter actualWriter = new PrintWriter(actualStringWriter);
            JAVAP_PROVIDER.run(actualWriter, actualWriter, command);
            final String actualJavap = actualStringWriter.toString();

            assertEquals(expectedJavap, actualJavap, () -> "Transformed class bytes did not match expected for " + name + ".class");
        } catch (final IOException exception) {
            exception.printStackTrace();
            System.err.println("Failed to diff class bytes using javap, falling back to direct byte comparison.");
            return false;
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
                // final boolean proceed = checkJavapDiff(name, expected.get(name), processed.get(name), Arrays.asList(JAVAP_PATH, "-c", "-p"));
                // verbose is too useful for invokedynamic debugging to omit
                checkJavapDiff(name, expected.get(name), processed.get(name), Arrays.asList("-c", "-p", "-v"));

                // If javap failed, just assert the bytes equal
                assertArrayEquals(
                    expected.get(name),
                    processed.get(name),
                    () -> "Transformed class bytes did not match expected for " + name + ".class"
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

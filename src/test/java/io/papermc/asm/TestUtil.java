package io.papermc.asm;

import io.papermc.asm.rules.RewriteRule;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
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
            final ClassWriter classWriter = new ClassWriter(classReader, 0);
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

    private static <T extends Throwable> void assertProcessedMatchesExpected_(
        final String className,
        final Processor<T> processor
    ) {
        final Map<String, byte[]> input = inputBytes(className);
        final Map<String, byte[]> processed = processClassBytes(input, processor);
        final Map<String, byte[]> expected;
        try {
            expected = expectedBytes(className);
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
            assertArrayEquals(
                expected.get(name),
                processed.get(name),
                () -> "Transformed class bytes did not match expected for " + name
            );
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
                    readClassBytes(map, innerNode.name, classNameMapper);
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}

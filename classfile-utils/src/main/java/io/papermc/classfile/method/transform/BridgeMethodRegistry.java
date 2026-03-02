package io.papermc.classfile.method.transform;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Collects bridge (synthetic) methods to be generated into the class currently being transformed.
 * Bridge methods are used when an invokedynamic instruction (e.g., a lambda or method reference)
 * needs an intermediate static method to perform a type conversion.
 *
 * <p>One instance is created per class transformation to avoid accumulating state across classes.</p>
 */
public final class BridgeMethodRegistry {

    private final Map<String, MethodGen> bridges = new LinkedHashMap<>();

    /**
     * Registers a bridge method to be generated. Returns the name of the registered method,
     * which may differ from {@code baseName} if a method with that name already exists.
     *
     * @param baseName suggested name for the bridge method
     * @param descriptor method descriptor (parameters and return type)
     * @param body code generator for the method body
     * @return the actual name assigned to the bridge method
     */
    public String registerBridge(final String baseName, final MethodTypeDesc descriptor, final Consumer<CodeBuilder> body) {
        String name = baseName;
        int counter = 0;
        while (this.bridges.containsKey(name)) {
            name = baseName + "$" + (++counter);
        }
        this.bridges.put(name, new MethodGen(descriptor, body));
        return name;
    }

    /**
     * Emits all registered bridge methods into the given class builder.
     * Called at the end of the class transformation.
     */
    public void emitAll(final ClassBuilder classBuilder) {
        for (final Map.Entry<String, MethodGen> entry : this.bridges.entrySet()) {
            final MethodGen gen = entry.getValue();
            classBuilder.withMethod(
                entry.getKey(),
                gen.descriptor(),
                ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC | ClassFile.ACC_SYNTHETIC,
                mb -> mb.withCode(gen.body())
            );
        }
    }

    public boolean isEmpty() {
        return this.bridges.isEmpty();
    }

    private record MethodGen(MethodTypeDesc descriptor, Consumer<CodeBuilder> body) {}
}

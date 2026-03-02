package io.papermc.classfile.method.transform;

import io.papermc.classfile.generation.ParameterGeneration;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static io.papermc.classfile.ClassFiles.GENERATED_PREFIX;
import static io.papermc.classfile.ClassFiles.toInternalName;

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
     * <p>The {@code return} will be done automatically, don't include it in the {@code Consumer}.</p>
     *
     * @param owner owner of the method
     * @param methodName name of the method
     * @param descriptor method descriptor (parameters and return type)
     * @param paramGeneration parameter generation helper
     * @param body code generator for the method body
     * @return the actual name assigned to the bridge method
     */
    public String registerBridge(final ClassDesc owner, final String methodName, final MethodTypeDesc descriptor, final ParameterGeneration paramGeneration, final Consumer<CodeBuilder> body) {
        final String baseName = GENERATED_PREFIX + toInternalName(owner).replace('/', '_') + '$' + methodName;
        String name = baseName;
        int counter = 0;
        while (this.bridges.containsKey(name)) {
            final MethodGen existing = this.bridges.get(name);
            if (existing.descriptor().equals(descriptor)) {
                // method's with the same descriptor should function the same
                return name;
            }
            name = baseName + "$" + (++counter);
        }
        final TypeKind returnTypeKind = TypeKind.from(descriptor.returnType());
        final Consumer<CodeBuilder> finalBuilder = builder -> {
            paramGeneration.generateParameters(descriptor, builder);
            body.accept(builder);
            builder.return_(returnTypeKind);
        };
        this.bridges.put(name, new MethodGen(descriptor, finalBuilder));
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

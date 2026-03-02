package io.papermc.classfile.generation;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

@FunctionalInterface
public interface ParameterGeneration {

    static ParameterGeneration standard() {
        return Standard.INSTANCE;
    }

    static ParameterGeneration standard(final Consumer<CodeBuilder> prefix) {
        return (descriptor, builder) -> {
            prefix.accept(builder);
            Standard.INSTANCE.generateParameters(descriptor, builder);
        };
    }

    static ParameterGeneration mutating(final Mutating mutator) {
        return mutating($ -> {}, mutator);
    }

    static ParameterGeneration mutating(final Consumer<CodeBuilder> prefix, final Mutating mutator) {
        return (descriptor, builder) -> {
            prefix.accept(builder);
            mutator.generateParameters(descriptor, builder);
        };
    }

    void generateParameters(MethodTypeDesc descriptor, CodeBuilder builder);

    record Standard() implements ParameterGeneration {

        private static final Standard INSTANCE = new Standard();

        @Override
        public void generateParameters(final MethodTypeDesc descriptor, final CodeBuilder builder) {
            // Load all parameters (using correct slots for wide types)
            int slot = 0;
            for (final ClassDesc paramType : descriptor.parameterList()) {
                final TypeKind typeKind = TypeKind.from(paramType);
                builder.loadLocal(typeKind, slot);
                slot += typeKind.slotSize();
            }
        }
    }

    @FunctionalInterface
    interface Mutating extends ParameterGeneration {

        @Override
        default void generateParameters(final MethodTypeDesc descriptor, final CodeBuilder builder) {
            // Load all parameters (using correct slots for wide types)
            int slot = 0;
            for (final ClassDesc paramType : descriptor.parameterList()) {
                final TypeKind typeKind = TypeKind.from(paramType);
                builder.loadLocal(typeKind, slot);
                this.mutate(paramType, builder);
                slot += typeKind.slotSize();
            }
        }

        void mutate(ClassDesc paramType, CodeBuilder builder);
    }
}

package io.papermc.classfile;

import io.papermc.classfile.method.transform.MethodTransformContext;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.LambdaMetafactory;
import java.util.Set;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

public final class ClassFiles {

    public static final int BOOTSTRAP_HANDLE_IDX = 1;
    public static final int DYNAMIC_TYPE_IDX = 2;
    public static final String CONSTRUCTOR_METHOD_NAME = "<init>";
    public static final ClassDesc LAMBDA_METAFACTORY = desc(LambdaMetafactory.class);
    public static final String GENERATED_PREFIX = "paperClassfileGenerated$";
    private static final String DEFAULT_CTOR_METHOD_PREFIX = "create";

    private ClassFiles() {
    }

    public static String toInternalName(final ClassDesc clazz) {
        if (!clazz.isClassOrInterface()) {
            throw new IllegalArgumentException("Not a class or interface: " + clazz);
        }
        return clazz.descriptorString().substring(1, clazz.descriptorString().length() - 1);
    }

    public static ClassDesc desc(final Class<?> clazz) {
        return clazz.describeConstable().orElseThrow();
    }

    public static MethodTypeDesc adjustForStatic(final Opcode opcode, final ClassDesc owner, final MethodTypeDesc descriptor) {
        return switch (opcode) {
            // for INVOKEVIRTUAL, INVOKEINTERFACE methods, we have to add the receiver as the first param for the static replacement
            case INVOKEVIRTUAL, INVOKEINTERFACE -> descriptor.insertParameterTypes(0, owner);
            // for INVOKESPECIAL, we have to add a return type; constructors have a void return type
            case INVOKESPECIAL -> descriptor.changeReturnType(owner);
            case INVOKESTATIC -> descriptor;
            default -> throw new IllegalArgumentException("Unexpected opcode: " + opcode);
        };
    }

    public static MethodTypeDesc adjustForStatic(final DirectMethodHandleDesc.Kind kind, final ClassDesc owner, final MethodTypeDesc descriptor) {
        return switch (kind) {
            // for VIRTUAL, INTERFACE_VIRTUAL methods, we have to add the receiver as the first param for the static replacement
            case VIRTUAL, INTERFACE_VIRTUAL -> descriptor.insertParameterTypes(0, owner);
            // for CONSTRUCTOR, we have to add a return type; constructors have a void return type
            case CONSTRUCTOR -> descriptor.changeReturnType(owner);
            case STATIC, INTERFACE_STATIC -> descriptor;
            default -> throw new IllegalArgumentException("Unexpected kind: " + kind);
        };
    }

    public static MethodTypeDesc replaceParameters(MethodTypeDesc descriptor, final Predicate<ClassDesc> oldParam, final ClassDesc newParam) {
        for (int i = 0; i < descriptor.parameterCount(); i++) {
            if (oldParam.test(descriptor.parameterType(i))) {
                descriptor = descriptor.changeParameterType(i, newParam);
            }
        }
        return descriptor;
    }

    public static String constructorMethodName(final ClassDesc owner) {
        // strip preceding "L" and trailing ";""
        final String ownerName = owner.descriptorString().substring(1, owner.descriptorString().length() - 1);
        return DEFAULT_CTOR_METHOD_PREFIX + ownerName.substring(ownerName.lastIndexOf('/') + 1);
    }

    public static void emitInvoke(final CodeBuilder cb, final Opcode opcode, final MethodTransformContext.MethodInfo info, final MethodTypeDesc callDesc, final boolean includeSpecial) {
        switch (opcode) {
            case INVOKEVIRTUAL -> cb.invokevirtual(info.owner(), info.name(), callDesc);
            case INVOKEINTERFACE -> cb.invokeinterface(info.owner(), info.name(), callDesc);
            case INVOKESTATIC -> cb.invokestatic(info.owner(), info.name(), callDesc, info.isInterface());
            case INVOKESPECIAL -> {
                if (!includeSpecial) {
                    throw new IllegalArgumentException("INVOKESPECIAL is not supported here");
                }
                cb.invokespecial(info.owner(), info.name(), callDesc, false);
            }
            default -> throw new IllegalArgumentException(opcode.toString());
        }
    }

    public static void emitInvoke(final CodeBuilder cb, final DirectMethodHandleDesc.Kind kind, final MethodTransformContext.MethodInfo info, final MethodTypeDesc callDesc, final boolean includeSpecial) {
        switch (kind) {
            case VIRTUAL -> cb.invokevirtual(info.owner(), info.name(), callDesc);
            case INTERFACE_VIRTUAL -> cb.invokeinterface(info.owner(), info.name(), callDesc);
            case STATIC -> cb.invokestatic(info.owner(), info.name(), callDesc, false);
            case INTERFACE_STATIC -> cb.invokestatic(info.owner(), info.name(), callDesc, true);
            case CONSTRUCTOR -> {
                if (!includeSpecial) {
                    throw new IllegalArgumentException("INVOKESPECIAL is not supported here");
                }
                cb.invokespecial(info.owner(), CONSTRUCTOR_METHOD_NAME, callDesc, false);
            }
            default -> throw new IllegalArgumentException(kind.toString());
        }
    }
}

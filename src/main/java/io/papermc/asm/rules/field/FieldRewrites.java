package io.papermc.asm.rules.field;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.builder.matcher.FieldMatcher;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;

public final class FieldRewrites {

    private static final ClassDesc VOID = void.class.describeConstable().orElseThrow();

    private FieldRewrites() {
    }

    public record ToMethodSameOwner(Set<ClassDesc> owners, FieldMatcher fieldMatcher, @Nullable String getterName, @Nullable String setterName, boolean isInterfaceMethod) implements FilteredFieldRewriteRule {

        public ToMethodSameOwner {
            if (getterName == null && setterName == null) {
                throw new IllegalArgumentException("At least one of getterName or setterName must be non-null");
            }
        }

        enum Type {
            GETTER {
                @Override
                int opcode(final int fieldOpcode) {
                    return switch (fieldOpcode) {
                        case Opcodes.GETFIELD -> Opcodes.INVOKEVIRTUAL;
                        case Opcodes.GETSTATIC -> Opcodes.INVOKESTATIC;
                        default -> throw new IllegalArgumentException("Unexpected opcode: " + fieldOpcode);
                    };
                }

                @Override
                MethodTypeDesc desc(final ClassDesc fieldTypeDesc) {
                    return MethodTypeDesc.of(fieldTypeDesc);
                }
            },
            SETTER {
                @Override
                int opcode(final int fieldOpcode) {
                    return switch (fieldOpcode) {
                        case Opcodes.PUTFIELD -> Opcodes.INVOKEVIRTUAL;
                        case Opcodes.PUTSTATIC -> Opcodes.INVOKESTATIC;
                        default -> throw new IllegalArgumentException("Unexpected opcode: " + fieldOpcode);
                    };
                }

                @Override
                MethodTypeDesc desc(final ClassDesc fieldTypeDesc) {
                    return MethodTypeDesc.of(VOID, fieldTypeDesc);
                }
            };

            abstract int opcode(final int fieldOpcode);

            abstract MethodTypeDesc desc(final ClassDesc fieldTypeDesc);
        }

        @Override
        public Rewrite rewrite(final ClassProcessingContext context, final int opcode, final String owner, final String name, final ClassDesc fieldTypeDesc) {
            return (delegate) -> {
                final Type type = switch (opcode) {
                    case Opcodes.GETFIELD, Opcodes.GETSTATIC -> Type.GETTER;
                    case Opcodes.PUTFIELD, Opcodes.PUTSTATIC -> Type.SETTER;
                    default -> throw new IllegalArgumentException("Unexpected opcode: " + opcode);
                };
                final @Nullable String methodName = switch (type) {
                    case GETTER -> this.getterName;
                    case SETTER -> this.setterName;
                };
                if (methodName == null) {
                    return;
                }

                delegate.visitMethodInsn(type.opcode(opcode), owner, methodName, type.desc(fieldTypeDesc).descriptorString(), this.isInterfaceMethod);
            };
        }
    }
}

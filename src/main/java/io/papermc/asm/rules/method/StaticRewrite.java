package io.papermc.asm.rules.method;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.builder.matcher.TargetedMethodMatcher;
import io.papermc.asm.rules.generate.GeneratedMethodHolder;
import io.papermc.asm.rules.generate.StaticRewriteGeneratedMethodHolder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static io.papermc.asm.util.DescriptorUtils.fromOwner;
import static io.papermc.asm.util.DescriptorUtils.replaceParameters;
import static io.papermc.asm.util.DescriptorUtils.toOwner;
import static io.papermc.asm.util.OpcodeUtils.isInterface;
import static io.papermc.asm.util.OpcodeUtils.isSpecial;
import static io.papermc.asm.util.OpcodeUtils.isStatic;
import static io.papermc.asm.util.OpcodeUtils.isVirtual;
import static io.papermc.asm.util.OpcodeUtils.staticOp;
import static java.util.function.Predicate.isEqual;

public interface StaticRewrite extends OwnableMethodRewriteRule.Filtered {

    String CONSTRUCTOR_METHOD_NAME = "<init>";
    String GENERATED_PREFIX = "paperAsmGenerated$";

    ClassDesc staticRedirectOwner(final ClassProcessingContext context);

    /**
     * Transforms the intermediate descriptor to the final
     * descriptor that will be used in the rewritten bytecode.
     * <p>
     * Intermediate means that it has been modified from the
     * original accounting for the virtual/interface/static/constructor-ness
     * of the method call.
     * </p>
     *
     * @param intermediateDescriptor the intermediate descriptor
     * @return the final descriptor to be used in the rewritten bytecode
     */
    default MethodTypeDesc transformToRedirectDescriptor(final MethodTypeDesc intermediateDescriptor) {
        return intermediateDescriptor;
    }

    /**
     * Creates a rewrite for the given method call data.
     *
     * @param context the context
     * @param intermediateDescriptor the descriptor modified from the original to account for the method call type (interface, virtual, static, etc.)
     * @param originalCallData the original method call data
     * @return the rewrite
     */
    default Rewrite<GeneratedMethodHolder.MethodCallData> createRewrite(final ClassProcessingContext context, final MethodTypeDesc intermediateDescriptor, final GeneratedMethodHolder.MethodCallData originalCallData) {
        return new RewriteSingle(staticOp(originalCallData.isInvokeDynamic()), this.staticRedirectOwner(context), originalCallData.name(), this.transformToRedirectDescriptor(intermediateDescriptor), false, originalCallData.isInvokeDynamic());
    }

    /**
     * Creates a rewrite for the given constructor call data.
     *
     * @param context the context
     * @param intermediateDescriptor the descriptor modified from the original to include a return type
     * @param originalCallData the original constructor call data
     * @return the rewrite
     */
    default Rewrite<GeneratedMethodHolder.ConstructorCallData> createConstructorRewrite(final ClassProcessingContext context, final MethodTypeDesc intermediateDescriptor, final GeneratedMethodHolder.ConstructorCallData originalCallData) {
        final String ownerString = toOwner(originalCallData.owner());
        final String staticMethodName = "create" + ownerString.substring(ownerString.lastIndexOf('/') + 1);
        return new RewriteConstructor(this.staticRedirectOwner(context), toOwner(originalCallData.owner()), staticMethodName, this.transformToRedirectDescriptor(intermediateDescriptor));
    }

    @Override
    default @Nullable Rewrite<?> rewrite(final ClassProcessingContext context, final boolean isInvokeDynamic, final int opcode, final ClassDesc owner, final String name, final MethodTypeDesc descriptor, final boolean isInterface) {
        MethodTypeDesc modifiedDescriptor = descriptor;
        if (isVirtual(opcode, isInvokeDynamic) || isInterface(opcode, isInvokeDynamic)) { // insert owner object as first param
            modifiedDescriptor = modifiedDescriptor.insertParameterTypes(0, owner);
        } else if (isSpecial(opcode, isInvokeDynamic)) {
            if (CONSTRUCTOR_METHOD_NAME.equals(name)) {
                modifiedDescriptor = modifiedDescriptor.changeReturnType(owner);
                return this.createConstructorRewrite(context, modifiedDescriptor, new GeneratedMethodHolder.ConstructorCallData(opcode, owner, descriptor));
            } else {
                throw new UnsupportedOperationException("Unhandled static rewrite: " + opcode + " " + owner + " " + name + " " + descriptor);
            }
        } else if (!isStatic(opcode, isInvokeDynamic)) {
            throw new UnsupportedOperationException("Unhandled static rewrite: " + opcode + " " + owner + " " + name + " " + descriptor);
        }
        return this.createRewrite(context, modifiedDescriptor, new GeneratedMethodHolder.MethodCallData(opcode, owner, name, descriptor, isInvokeDynamic));
    }

    record RewriteConstructor(ClassDesc staticRedirectOwner, String constructorOwner, String methodName, MethodTypeDesc descriptor, @Nullable GeneratorInfo<GeneratedMethodHolder.ConstructorCallData> generatorInfo) implements MethodRewriteRule.Rewrite<GeneratedMethodHolder.ConstructorCallData> {

        public RewriteConstructor(final ClassDesc staticRedirectOwner, final String constructorOwner, final String methodName, final MethodTypeDesc descriptor) {
            this(staticRedirectOwner, constructorOwner, methodName, descriptor, null);
        }

        @Override
        public void apply(final MethodVisitor delegate, final MethodNode context) {
            // the bytecode instruction sequence for constructors is:
            // NEW <owner>
            // DUP
            // <other instructions that might set up parameters>
            // INVOKESPECIAL <owner>/<init>
            // This detects that pattern and correctly removes instructions that
            // are no longer needed after the static redirect
            AbstractInsnNode insn = context.instructions.getLast();
            boolean lastInsnWasDup = false;
            boolean handled = false;
            final Deque<String> typeStack = new ArrayDeque<>();
            while (insn != null) {
                if (insn.getOpcode() == Opcodes.INVOKESPECIAL && CONSTRUCTOR_METHOD_NAME.equals(((MethodInsnNode) insn).name)) {
                    typeStack.push(((MethodInsnNode) insn).owner);
                }
                if (lastInsnWasDup && insn.getOpcode() == Opcodes.NEW) {
                    final TypeInsnNode newNode = (TypeInsnNode) insn;
                    if (typeStack.isEmpty()) {
                        if (!newNode.desc.equals(this.constructorOwner())) {
                            throw new IllegalStateException("typeStack was empty and the 'new' type didn't match the ctor type");
                        }
                        final AbstractInsnNode dup = insn.getNext();
                        context.instructions.remove(insn); // remove NEW
                        context.instructions.remove(dup); // remove DUP
                        handled = true;
                        break;
                    } else {
                        final String top = typeStack.pop();
                        if (!newNode.desc.equals(top)) {
                            throw new IllegalStateException("typeStack top " + top + " didn't match expected " + newNode.desc + " from 'new' node");
                        }
                    }
                }
                lastInsnWasDup = insn.getOpcode() == Opcodes.DUP;
                insn = insn.getPrevious();
            }
            if (!handled) {
                throw new IllegalStateException("Didn't find new/dup before invokespecial for ctor");
            }
            delegate.visitMethodInsn(Opcodes.INVOKESTATIC, toOwner(this.staticRedirectOwner()), this.methodName(), this.descriptor().descriptorString(), false);
        }

        @Override
        public Handle createHandle() {
            return new Handle(Opcodes.H_INVOKESTATIC, toOwner(this.staticRedirectOwner()), this.methodName(), this.descriptor().descriptorString(), false);
        }

        @Override
        public Rewrite<GeneratedMethodHolder.ConstructorCallData> withNamePrefix(final String prefix) {
            return new RewriteConstructor(this.staticRedirectOwner(), this.constructorOwner(), prefix + this.methodName(), this.descriptor(), this.generatorInfo());
        }

        @Override
        public Rewrite<GeneratedMethodHolder.ConstructorCallData> withGeneratorInfo(final GeneratedMethodHolder holder, final GeneratedMethodHolder.ConstructorCallData original) {
            return new RewriteConstructor(this.staticRedirectOwner(), this.constructorOwner(), this.methodName(), this.descriptor(), new GeneratorInfo<>(holder, original));
        }

        @Override
        public @Nullable MethodGenerator createMethodGenerator() {
            if (this.generatorInfo == null) {
                return null;
            }
            final GeneratedMethodHolder.ConstructorCallData original = this.generatorInfo.original();
            return factory -> {
                this.generatorInfo.holder().generateConstructor(
                    factory,
                    new GeneratedMethodHolder.MethodCallData(Opcodes.INVOKESTATIC, this.staticRedirectOwner(), this.methodName(), this.descriptor(), false),
                    original
                );
            };
        }
    }

    interface Generated extends StaticRewrite, GeneratedMethodHolder {

        /**
         * Gets the "new" type that exists in the current source.
         *
         * @return the "new" type from source
         */
        ClassDesc existingType();

        /**
         * Gets the targeted method matcher for the rewrite.
         *
         * @return the targeted method matcher
         */
        TargetedMethodMatcher methodMatcher();

        @Override
        default ClassDesc staticRedirectOwner(final ClassProcessingContext context) {
            return fromOwner(context.processingClassName()); // create generated method in current class
        }

        @Override
        default Rewrite<MethodCallData> createRewrite(final ClassProcessingContext context, final MethodTypeDesc intermediateDescriptor, final MethodCallData originalCallData) {
            return StaticRewrite.super.createRewrite(context, intermediateDescriptor, originalCallData)
                .withNamePrefix(GENERATED_PREFIX)
                .withGeneratorInfo(this, originalCallData);
        }

        @Override
        default Rewrite<ConstructorCallData> createConstructorRewrite(final ClassProcessingContext context, final MethodTypeDesc intermediateDescriptor, final ConstructorCallData originalCallData) {
            return StaticRewrite.super.createConstructorRewrite(context, intermediateDescriptor, originalCallData)
                .withNamePrefix(GENERATED_PREFIX)
                .withGeneratorInfo(this, originalCallData);
        }

        interface Param extends Generated, StaticRewriteGeneratedMethodHolder.Param {

            @Override
            default MethodTypeDesc transformInvokedDescriptor(final MethodTypeDesc original, final Set<Integer> context) {
                // To create the generated method descriptor from the existing (in source) descriptor, we replace the
                // legacy param with the new param
                return replaceParameters(original, isEqual(this.methodMatcher().targetType()), this.existingType(), context);
            }
        }

        interface Return extends Generated, StaticRewriteGeneratedMethodHolder.Return {

            @Override
            default MethodTypeDesc transformInvokedDescriptor(final MethodTypeDesc original, final Void context) {
                return original.changeReturnType(this.existingType());
            }
        }
    }

}

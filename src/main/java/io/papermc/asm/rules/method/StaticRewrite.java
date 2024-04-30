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

public interface StaticRewrite extends FilteredMethodRewriteRule {

    String CONSTRUCTOR_METHOD_NAME = "<init>";

    ClassDesc staticRedirectOwner(final ClassProcessingContext context);

    /**
     * Transforms the intermediate descriptor to the final
     * descriptor that will be used in the rewritten bytecode.
     * <p>
     * Intermediate means that it has been modified from the
     * original accounting for the virtual/interface/static/constructor-ness
     * of the method call.
     *
     * @param intermediateDescriptor the intermediate descriptor
     * @return the final descriptor to be used in the rewritten bytecode
     */
    default MethodTypeDesc transformToRedirectDescriptor(final MethodTypeDesc intermediateDescriptor) {
        return intermediateDescriptor;
    }

    default Rewrite createRewrite(final ClassProcessingContext context, final MethodTypeDesc descriptor, final GeneratedMethodHolder.MethodCallData callData) {
        return new RewriteSingle(staticOp(callData.isInvokeDynamic()), this.staticRedirectOwner(context), callData.name(), this.transformToRedirectDescriptor(descriptor), false, callData.isInvokeDynamic());
    }

    default Rewrite createConstructorRewrite(final ClassProcessingContext context, final String name, final MethodTypeDesc descriptor, final GeneratedMethodHolder.ConstructorCallData callData) {
        return new RewriteConstructor(this.staticRedirectOwner(context), toOwner(callData.owner()), name, this.transformToRedirectDescriptor(descriptor));
    }

    @Override
    default @Nullable Rewrite rewrite(final ClassProcessingContext context, final boolean isInvokeDynamic, final int opcode, final ClassDesc owner, final String name, final MethodTypeDesc descriptor, final boolean isInterface) {
        MethodTypeDesc modifiedDescriptor = descriptor;
        if (isVirtual(opcode, isInvokeDynamic) || isInterface(opcode, isInvokeDynamic)) { // insert owner object as first param
            modifiedDescriptor = modifiedDescriptor.insertParameterTypes(0, owner);
        } else if (isSpecial(opcode, isInvokeDynamic)) {
            if (CONSTRUCTOR_METHOD_NAME.equals(name)) {
                final String ownerString = toOwner(owner);
                final String newName = "create" + ownerString.substring(ownerString.lastIndexOf('/') + 1);
                modifiedDescriptor = modifiedDescriptor.changeReturnType(owner);
                return this.createConstructorRewrite(context, newName, modifiedDescriptor, new GeneratedMethodHolder.ConstructorCallData(opcode, owner, descriptor));
            } else {
                throw new UnsupportedOperationException("Unhandled static rewrite: " + opcode + " " + owner + " " + name + " " + descriptor);
            }
        } else if (!isStatic(opcode, isInvokeDynamic)) {
            throw new UnsupportedOperationException("Unhandled static rewrite: " + opcode + " " + owner + " " + name + " " + descriptor);
        }
        return this.createRewrite(context, modifiedDescriptor, new GeneratedMethodHolder.MethodCallData(opcode, owner, name, descriptor, isInvokeDynamic));
    }

    record RewriteConstructor(ClassDesc staticRedirectOwner, String constructorOwner, String methodName, MethodTypeDesc descriptor, @Nullable MethodGeneratorFactory methodGeneratorFactory) implements MethodRewriteRule.Rewrite {

        public RewriteConstructor(final ClassDesc staticRedirectOwner, final String constructorOwner, final String methodName, final MethodTypeDesc descriptor) {
            this(staticRedirectOwner, constructorOwner, methodName, descriptor, null);
        }

        @Override
        public void apply(final MethodVisitor delegate, final MethodNode context) {
            AbstractInsnNode insn = context.instructions.getLast();
            boolean wasDup = false;
            boolean handled = false;
            final Deque<String> typeStack = new ArrayDeque<>();
            while (insn != null) {
                if (insn.getOpcode() == Opcodes.INVOKESPECIAL && CONSTRUCTOR_METHOD_NAME.equals(((MethodInsnNode) insn).name)) {
                    typeStack.push(((MethodInsnNode) insn).owner);
                }
                if (wasDup && insn.getOpcode() == Opcodes.NEW) {
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
                wasDup = insn.getOpcode() == Opcodes.DUP;
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
        public GeneratedMethodHolder.MethodCallData createModifiedData() {
            return new GeneratedMethodHolder.MethodCallData(Opcodes.INVOKESTATIC, this.staticRedirectOwner(), this.methodName(), this.descriptor(), false);
        }

        @Override
        public Rewrite withFactory(final MethodGeneratorFactory factoryFactory) {
            return new RewriteConstructor(this.staticRedirectOwner(), this.constructorOwner(), this.methodName(), this.descriptor(), factoryFactory);
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
        default Rewrite createRewrite(final ClassProcessingContext context, final MethodTypeDesc descriptor, final MethodCallData callData) {
            return StaticRewrite.super.createRewrite(context, descriptor, callData).withFactory(modified -> {
                return factory -> this.generateMethod(factory, modified, callData);
            });
        }

        @Override
        default Rewrite createConstructorRewrite(final ClassProcessingContext context, final String name, final MethodTypeDesc descriptor, final ConstructorCallData callData) {
            return StaticRewrite.super.createConstructorRewrite(context, name, descriptor, callData).withFactory(modified -> {
                return factory -> this.generateConstructor(factory, modified, callData);
            });
        }

        interface Param extends Generated, StaticRewriteGeneratedMethodHolder.Param {

            @Override
            default MethodTypeDesc transformInvokedDescriptor(final MethodTypeDesc original, final Set<Integer> context) {
                // To create the generated method descriptor from the existing (in source) descriptor, we replace the
                // existing type with the fuzzy param
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

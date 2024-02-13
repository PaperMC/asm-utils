package io.papermc.asm.rules.method;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import io.papermc.asm.rules.generate.GeneratedMethodHolder;
import io.papermc.asm.rules.generate.StaticRewriteGeneratedMethodHolder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static io.papermc.asm.util.DescriptorUtils.fromOwner;
import static io.papermc.asm.util.DescriptorUtils.toOwner;
import static io.papermc.asm.util.OpcodeUtils.isInterface;
import static io.papermc.asm.util.OpcodeUtils.isSpecial;
import static io.papermc.asm.util.OpcodeUtils.isStatic;
import static io.papermc.asm.util.OpcodeUtils.isVirtual;
import static io.papermc.asm.util.OpcodeUtils.staticOp;
import static java.util.function.Predicate.isEqual;

public interface StaticRewrite extends FilteredMethodRewriteRule {

    ClassDesc staticRedirectOwner();

    default MethodTypeDesc modifyMethodDescriptor(final MethodTypeDesc bytecodeDescriptor) {
        return bytecodeDescriptor;
    }

    @Override
    default MethodRewriteRule.Rewrite rewrite(final ClassProcessingContext context, final boolean invokeDynamic, final int opcode, final String owner, String name, MethodTypeDesc descriptor, final boolean isInterface) {
        if (isVirtual(opcode, invokeDynamic) || isInterface(opcode, invokeDynamic)) { // insert owner object as first param
            descriptor = descriptor.insertParameterTypes(0, fromOwner(owner));
        } else if (isSpecial(opcode, invokeDynamic)) {
            if ("<init>".equals(name)) {
                name = "create" + owner.substring(owner.lastIndexOf('/') + 1);
                descriptor = descriptor.changeReturnType(fromOwner(owner));
                return new RewriteConstructor(this.staticRedirectOwner(), owner, name, this.modifyMethodDescriptor(descriptor));
            } else {
                throw new UnsupportedOperationException("Unhandled static rewrite: " + opcode + " " + owner + " " + name + " " + descriptor);
            }
        } else if (!isStatic(opcode, invokeDynamic)) {
            throw new UnsupportedOperationException("Unhandled static rewrite: " + opcode + " " + owner + " " + name + " " + descriptor);
        }
        return new RewriteSingle(staticOp(invokeDynamic), toOwner(this.staticRedirectOwner()), name, this.modifyMethodDescriptor(descriptor), false);
    }

    record RewriteConstructor(ClassDesc staticRedirectOwner, String constructorOwner, String methodName, MethodTypeDesc descriptor) implements MethodRewriteRule.Rewrite {

        @Override
        public void apply(final MethodVisitor delegate, final MethodNode context) {
            AbstractInsnNode insn = context.instructions.getLast();
            boolean wasDup = false;
            boolean handled = false;
            final Deque<String> typeStack = new ArrayDeque<>();
            while (insn != null) {
                if (insn.getOpcode() == Opcodes.INVOKESPECIAL && "<init>".equals(((MethodInsnNode) insn).name)) {
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
    }

    interface Generated extends StaticRewrite, GeneratedMethodHolder {

        // used to search the owning classes for matching existing methods to template the generated methods
        ClassDesc existingType();

        @Override
        default ClassDesc staticRedirectOwner() {
            return GeneratedMethodHolder.super.staticRedirectOwner();
        }

        @Override
        default void generateMethods(final MethodGeneratorFactory methodGeneratorFactory) {
            this.matchingMethodsByName().filter(pair -> {
                return this.matchesExistingMethod(pair.getValue());
            }).forEach(pair -> this.generateMethod(pair, methodGeneratorFactory));
        }

        boolean matchesExistingMethod(MethodTypeDesc desc);

        interface Param extends Generated, StaticRewriteGeneratedMethodHolder.Param {

            @Override
            default boolean matchesExistingMethod(final MethodTypeDesc desc) {
                return desc.parameterList().stream().anyMatch(isEqual(this.existingType()));
            }
        }

        interface Return extends Generated, StaticRewriteGeneratedMethodHolder.Return {

            @Override
            default boolean matchesExistingMethod(final MethodTypeDesc desc) {
                return desc.returnType().equals(this.existingType());
            }
        }
    }

    // does a plain static rewrite with exact matching parameters
    record Plain(Set<Class<?>> owners, MethodMatcher methodMatcher, ClassDesc staticRedirectOwner) implements StaticRewrite {
    }
}

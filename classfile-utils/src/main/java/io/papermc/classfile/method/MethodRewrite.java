package io.papermc.classfile.method;

import io.papermc.classfile.method.action.MethodRewriteAction;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import static io.papermc.classfile.ClassfileUtils.desc;

public record MethodRewrite(ClassDesc owner, MethodNamePredicate methodName, MethodDescriptorPredicate descriptor, MethodRewriteAction action) implements CodeTransform {

    private static final ClassDesc LAMBDA_METAFACTORY = desc(LambdaMetafactory.class);

    public boolean requiresMethodTransform() {
        return this.methodName().test("<init>");
    }

    boolean transformInvoke(final CodeBuilder builder, final InvokeInstruction invoke) {
        final ClassDesc methodOwner = invoke.owner().asSymbol();
        if (!this.owner.equals(methodOwner)) {
            return false;
        }
        if (!this.methodName.test(invoke.name())) {
            return false;
        }
        if (!this.descriptor.test(invoke.typeSymbol())) {
            return false;
        }
        if (invoke.opcode() == Opcode.INVOKESPECIAL) {
            throw new UnsupportedOperationException("You cannot redirect INVOKESPECIAL here");
        }
        this.action.rewriteInvoke(builder::with, builder.constantPool(), invoke.opcode(), methodOwner, invoke.name().stringValue(), invoke.typeSymbol());
        return true;
    }

    boolean transformInvokeDynamic(final CodeBuilder builder, final InvokeDynamicInstruction invokeDynamic) {
        final DirectMethodHandleDesc bootstrapMethod = invokeDynamic.bootstrapMethod();
        final List<ConstantDesc> args = invokeDynamic.bootstrapArgs();
        if (!bootstrapMethod.owner().equals(LAMBDA_METAFACTORY) || args.size() < 2) {
            // only looking for lambda metafactory calls
            return false;
        }
        if (!(args.get(1) instanceof final DirectMethodHandleDesc methodHandle)) {
            return false;
        }
        if (!this.owner.equals(methodHandle.owner())) {
            return false;
        }
        if (!this.methodName.test(methodHandle.methodName())) {
            return false;
        }
        if (!this.descriptor.test(methodHandle.invocationType())) {
            return false;
        }
        final MethodRewriteAction.BootstrapInfo info = new MethodRewriteAction.BootstrapInfo(bootstrapMethod, invokeDynamic.name().stringValue(), invokeDynamic.typeSymbol(), args);
        this.action.rewriteInvokeDynamic(builder,
            methodHandle.kind(),
            methodHandle.owner(),
            methodHandle.methodName(),
            methodHandle.invocationType(),
            info
        );
        return true;
    }

    @Override
    public void accept(final CodeBuilder builder, final CodeElement element) {
        final boolean written = switch (element) {
            case final InvokeInstruction invoke -> this.transformInvoke(builder, invoke);
            case final InvokeDynamicInstruction invokeDynamic -> this.transformInvokeDynamic(builder, invokeDynamic);
            default -> false;
        };
        if (!written) {
            builder.with(element);
        }
    }

    public CodeTransform newConstructorTransform() {
        return new ConstructorRewriteTransform();
    }

    private final class ConstructorRewriteTransform implements CodeTransform {
        private final Deque<List<CodeElement>> bufferStack = new ArrayDeque<>();

        @Override
        public void accept(final CodeBuilder builder, final CodeElement element) {
            if (element instanceof NewObjectInstruction) {
                // start of a constructor level
                this.bufferStack.push(new ArrayList<>(List.of(element)));
                return;
            }

            if (!this.bufferStack.isEmpty()) {
                this.bufferStack.peek().add(element);

                if (element instanceof final InvokeInstruction invoke && invoke.opcode() == Opcode.INVOKESPECIAL && invoke.name().equalsString(MethodRewriteAction.CONSTRUCTOR_METHOD_NAME)) {
                    // end of a constructor level
                    final List<CodeElement> level = this.bufferStack.pop();

                    final List<CodeElement> updatedLevel;
                    if (invoke.owner().matches(MethodRewrite.this.owner()) && MethodRewrite.this.methodName.test(invoke.name()) && MethodRewrite.this.descriptor.test(invoke.typeSymbol())) {
                        // matches our instruction to be removed
                        // we are removing the POP and NEW instructions here (first 2)
                        // AND the INVOKESPECIAL that was added a few lines above (last)
                        updatedLevel = new ArrayList<>(level.subList(2, level.size() - 1));
                        MethodRewrite.this.action().rewriteInvoke(
                            updatedLevel::add,
                            builder.constantPool(),
                            invoke.opcode(),
                            invoke.owner().asSymbol(),
                            invoke.name().stringValue(),
                            invoke.typeSymbol()
                        );
                    } else {
                        updatedLevel = level;
                    }
                    if (!this.bufferStack.isEmpty()) {
                        this.bufferStack.peek().addAll(updatedLevel);
                    } else {
                        this.flushLevel(builder, updatedLevel);
                    }
                }
                return;
            }
            this.writeToBuilder(builder, element);
        }

        @Override
        public void atEnd(final CodeBuilder builder) {
            // Drain stack bottom-up
            final List<List<CodeElement>> remaining = new ArrayList<>(this.bufferStack);
            Collections.reverse(remaining);
            remaining.forEach(level -> this.flushLevel(builder, level));
            this.bufferStack.clear();
        }

        private void flushLevel(final CodeBuilder builder, final List<CodeElement> level) {
            level.forEach(el -> this.writeToBuilder(builder, el));
        }

        private void writeToBuilder(final CodeBuilder builder, final CodeElement element) {
            // anytime we write to the builder, we first need to check that
            // we don't need to also rewrite this instruction
            MethodRewrite.this.accept(builder, element);
        }

    }
}

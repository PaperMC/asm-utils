package io.papermc.classfile.method;

import io.papermc.classfile.method.action.MethodRewriteAction;
import io.papermc.classfile.method.transform.ConstructorAwareCodeTransform;
import io.papermc.classfile.method.transform.SimpleMethodBodyTransform;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.Opcode;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.invoke.LambdaMetafactory;
import java.util.List;
import java.util.function.Consumer;

import static io.papermc.classfile.ClassFiles.desc;

public record MethodRewrite(ClassDesc owner, MethodNamePredicate methodName, MethodDescriptorPredicate descriptor, MethodRewriteAction action) {

    public boolean transformInvoke(
        final Consumer<CodeElement> emit,
        final ConstantPoolBuilder poolBuilder,
        final ClassDesc methodOwner,
        final String methodName,
        final InvokeInstruction invoke
    ) {
        // owner validated by caller
        if (!this.methodName.test(methodName)) {
            return false;
        }
        if (!this.descriptor.test(invoke.typeSymbol())) {
            return false;
        }
        final CheckedConsumer<CodeElement> checkedEmit = new CheckedConsumer<>(emit);
        this.action.rewriteInvoke(checkedEmit, poolBuilder, invoke.opcode(), methodOwner, invoke.name().stringValue(), invoke.typeSymbol());
        checkedEmit.verify();
        return true;
    }

    public boolean transformInvokeDynamic(
        final Consumer<CodeElement> emit,
        final ConstantPoolBuilder poolBuilder,
        final DirectMethodHandleDesc bootstrapMethod,
        final DirectMethodHandleDesc methodHandle,
        final List<ConstantDesc> args,
        final InvokeDynamicInstruction invokeDynamic
    ) {
        // owner validated by caller
        if (!this.methodName.test(methodHandle.methodName())) {
            return false;
        }
        if (!this.descriptor.test(methodHandle.invocationType())) {
            return false;
        }
        final MethodRewriteAction.BootstrapInfo info = new MethodRewriteAction.BootstrapInfo(bootstrapMethod, invokeDynamic.name().stringValue(), invokeDynamic.typeSymbol(), args);
        final CheckedConsumer<CodeElement> checkedEmit = new CheckedConsumer<>(emit);
        this.action.rewriteInvokeDynamic(checkedEmit, poolBuilder, methodHandle.kind(), methodHandle.owner(), methodHandle.methodName(), methodHandle.invocationType(), info);
        checkedEmit.verify();
        return true;
    }

    public static ClassTransform createTransform(final MethodRewriteIndex index) {
        final SimpleMethodBodyTransform basicTransform = new SimpleMethodBodyTransform(index);
        if (!index.hasConstructorRewrites()) {
            return ClassTransform.transformingMethodBodies(basicTransform);
        }
        return ClassTransform.transformingMethodBodies(CodeTransform.ofStateful(() -> {
            return new ConstructorAwareCodeTransform(index, basicTransform);
        }));
    }

    private static final class CheckedConsumer<T> implements Consumer<T> {

        private final Consumer<T> wrapped;
        boolean called = false;

        private CheckedConsumer(final Consumer<T> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void accept(final T t) {
            this.wrapped.accept(t);
            this.called = true;
        }

        public void verify() {
            if (!this.called) {
                throw new IllegalStateException("Consumer was not called");
            }
        }
    }
}

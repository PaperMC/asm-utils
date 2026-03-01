package io.papermc.classfile.method.transform;

import io.papermc.classfile.ClassFiles;
import io.papermc.classfile.method.MethodRewrite;
import io.papermc.classfile.method.MethodRewriteIndex;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * This is a CodeTransform that is aware of constructors and
 * delays writing all instructions between (inclusive) the NEW and INVOKESPECIAL
 * that bound complied constructor invocations. This is done so that they
 * can be selectively removed if required.
 */
public class ConstructorAwareCodeTransform implements CodeTransform {
    
    private final Deque<Level> bufferStack = new ArrayDeque<>();
    private final MethodRewriteIndex index;
    private final CodeTransform fallbackTransform;

    public ConstructorAwareCodeTransform(final MethodRewriteIndex index, final CodeTransform fallbackTransform) {
        this.index = index;
        this.fallbackTransform = fallbackTransform;
    }

    @Override
    public void accept(final CodeBuilder builder, final CodeElement element) {
        if (element instanceof NewObjectInstruction) {
            // start of a constructor level
            final Level level = new Level();
            level.add(element);
            this.bufferStack.push(level);
            return;
        }

        if (!this.bufferStack.isEmpty()) {
            // avoid the wrong inspection at the "add" below saying this can be null
            final Level peekedLevel = this.bufferStack.peek();
            if (isConstructor(element)) {
                // end of a constructor level
                final InvokeInstruction invoke = (InvokeInstruction) element;
                final Level level = this.bufferStack.pop();
                final MethodTransforms.BoundRewrite boundRewrite = MethodTransforms.setupRewrite(invoke);
                if (boundRewrite == null) {
                    // should rarely happen, if ever. Only for some different form of LambdaMetafactory call
                    level.add(element);
                    return;
                }
                final List<MethodRewrite> candidates = this.index.constructorCandidates(invoke.owner().asSymbol());
                MethodTransforms.writeFromCandidates(
                    candidates,
                    builder.constantPool(),
                    invoke,
                    boundRewrite,
                    el -> {
                        // only strip out when we know we are writing the changed instruction
                        level.stripOutBadInstructions();
                        level.addDirect(el);
                    },
                    level::add
                );
                // the instruction, either original or modified, should always be added to the level by this point

                if (!this.bufferStack.isEmpty()) {
                    this.bufferStack.peek().addAllFrom(level);
                } else {
                    level.flush(builder, this.fallbackTransform::accept);
                }
            } else {
                peekedLevel.add(element);
            }
            return;
        }

        // anytime we write to the builder, we first need to check that
        // we don't need to also rewrite this instruction
        this.fallbackTransform.accept(builder, element);
    }

    @Override
    public void atEnd(final CodeBuilder builder) {
        // Drain stack bottom-up
        final List<Level> remaining = new ArrayList<>(this.bufferStack);
        Collections.reverse(remaining);
        remaining.forEach(level -> level.flush(builder, this.fallbackTransform::accept));
        this.bufferStack.clear();
    }

    static boolean isConstructor(final CodeElement element) {
        if (!(element instanceof final InvokeInstruction invoke)) {
            return false;
        }
        return invoke.opcode() == Opcode.INVOKESPECIAL && invoke.method().name().equalsString(ClassFiles.CONSTRUCTOR_METHOD_NAME);
    }

    private sealed interface LevelElement {

        record PassThrough(CodeElement element) implements LevelElement {}

        record Direct(CodeElement element) implements LevelElement {}
    }
    
    private record Level(List<LevelElement> elements) {

        Level() {
            this(new ArrayList<>());
        }

        void stripOutBadInstructions() {
            // we are removing the POP and NEW instructions here (first 2)
            this.elements.removeFirst();
            this.elements.removeFirst();
        }

        void add(final CodeElement element) {
            this.add(new LevelElement.PassThrough(element));
        }

        void addDirect(final CodeElement element) {
            this.add(new LevelElement.Direct(element));
        }

        void add(final LevelElement element) {
            this.elements.add(element);
        }

        void addAllFrom(final Level other) {
            this.elements.addAll(other.elements);
        }

        void flush(final CodeBuilder builder, final BiConsumer<CodeBuilder, CodeElement> rewriteInvoke) {
            for (final LevelElement element : this.elements) {
                switch (element) {
                    case final LevelElement.PassThrough pass -> rewriteInvoke.accept(builder, pass.element());
                    case final LevelElement.Direct direct -> builder.with(direct.element());
                }
            }
        }
    }
}

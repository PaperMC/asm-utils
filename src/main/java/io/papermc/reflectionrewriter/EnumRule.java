package io.papermc.reflectionrewriter;

import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Rule for rewriting enum valueOf calls. Not normally needed
 * as all common remapping software only remaps enum field names,
 * not the ldc insn for the name of the enum constant.
 *
 * <p>This rule expects the following methods to be present
 * on your reflection proxy class.</p>
 *
 * <pre>
 * public static &#60;E extends Enum&#60;E>> E enumConstant(
 *     final MethodHandles.Lookup lookup,
 *     final String name,
 *     final Class&#60;E> type
 * ) {
 *     // Implementation ...
 * }
 *
 * public static &#60;E extends Enum&#60;E>> E valueOf(
 *     final Class&#60;E> enumClass,
 *     final String name
 * ) {
 *     // Implementation...
 * }
 *
 * public static &#60;E extends Enum&#60;E>> E valueOf(
 *     final String name,
 *     final Class&#60;E> enumClass
 * ) {
 *     return valueOf(enumClass, name);
 * }
 * </pre>
 */
public final class EnumRule {
    private EnumRule() {
    }

    public static RewriteRule create(
        final String proxyClassName,
        final ClassInfoProvider classInfoProvider,
        final Predicate<String> ownerPredicate
    ) {
        final RewriteRule enumConstantRule = RewriteRule.methodVisitorBuilder(builder -> builder.visitBoth(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/ConstantBootstraps",
            (parent0, owner, name, descriptor, isInterface) -> {
                if (name.equals("enumConstant") && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Enum;")) {
                    return InvokeStaticRewrite.staticRedirect(proxyClassName, name, descriptor);
                }
                return null;
            }
        )));
        return new RewriteRule((api, parent) -> enumConstantRule.methodVisitorFactory().createVisitor(
            api,
            new EnumMethodVisitor(api, parent, proxyClassName, classInfoProvider, ownerPredicate)
        ));
    }

    public static RewriteRule minecraft(
        final String proxyClassName,
        final ClassInfoProvider classInfoProvider
    ) {
        return create(proxyClassName, classInfoProvider, owner -> owner.startsWith("net/minecraft/") || owner.startsWith("com/mojang/"));
    }

    // todo needs to handle invokedynamic?
    private static final class EnumMethodVisitor extends MethodVisitor {
        private final String proxy;
        private final ClassInfoProvider classInfoProvider;
        private final Predicate<String> ownerPredicate;
        private int increaseMaxStack;

        EnumMethodVisitor(final int api, final MethodVisitor parent, final String proxyClassName, final ClassInfoProvider classInfoProvider, final Predicate<String> ownerPredicate) {
            super(api, parent);
            this.proxy = proxyClassName;
            this.classInfoProvider = classInfoProvider;
            this.ownerPredicate = ownerPredicate;
        }

        @Override
        public void visitMaxs(final int maxStack, final int maxLocals) {
            super.visitMaxs(maxStack + this.increaseMaxStack, maxLocals);
            this.increaseMaxStack = 0;
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
            if (this.ownerPredicate.test(owner) && name.equals("valueOf") && descriptor.equals("(Ljava/lang/String;)L" + owner + ";")) {
                // Rewrite SomeEnum.valueOf(String)
                final @Nullable ClassInfo info = this.classInfoProvider.info(owner);
                if (info != null && info.isEnum()) {
                    // Increase max stack size for this method by one for the class parameter
                    // Note that in some cases this does not actually have to be increased (which we cannot track here),
                    // but a larger max stack size than needed should not create any problems
                    this.increaseMaxStack++;

                    super.visitLdcInsn(Type.getType("L" + owner + ";")); // Add the class as a parameter
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, this.proxy, name, "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Enum;", false);
                    super.visitTypeInsn(Opcodes.CHECKCAST, owner); // Make sure we have the right type
                    return;
                }
            } else if (name.equals("valueOf") && descriptor.equals("(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;")) {
                // Rewrite AnyEnum.valueOf(Class, String)
                if (this.isEnum(owner)) {
                    super.visitMethodInsn(opcode, this.proxy, name, descriptor, false);
                    return;
                }
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        private boolean isEnum(final String owner) {
            if (owner.equals("java/lang/Enum")) {
                return true;
            }
            final @Nullable ClassInfo info = this.classInfoProvider.info(owner);
            return info != null && info.isEnum();
        }
    }
}

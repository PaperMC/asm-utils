package io.papermc.reflectionrewriter;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class BuiltInRules {
    private final String proxy;
    private final RewriteRule classRule;
    // TODO MethodHandles$Lookup: findVarHandle, findStaticGetter, findStaticSetter, findStaticVarHandle, bind
    private final RewriteRule methodHandlesLookupRule;
    private final RewriteRule lambdaMetafactoryRule;
    // TODO ConstantBootstraps: fieldVarHandle, staticFieldVarHandle
    private final RewriteRule constantBootstrapsRule;
    // TODO Enums (google), EnumUtils (commons), any other enum and reflection utils bundled with Paper
    private final RewriteRule methodTypeRule;
    private final RewriteRule enumRule;
    // TODO jdk.internal.Unsafe#objectFieldOffset (and defineClass?)
    // TODO ClassLoader#defineClass?

    public BuiltInRules(final String proxyClassName, final ClassInfoProvider classInfoProvider) {
        this.proxy = proxyClassName;
        this.classRule = this.createClassRule();
        this.methodHandlesLookupRule = this.createMethodHandlesLookupRule();
        this.lambdaMetafactoryRule = this.createLamdaMetafactoryRule();
        this.constantBootstrapsRule = this.createConstantBootstrapsRule();
        this.methodTypeRule = this.createMethodTypeRule();
        this.enumRule = new RewriteRule((api, parent) -> new EnumMethodVisitor(api, parent, proxyClassName, classInfoProvider));
    }

    public RewriteRule classRule() {
        return this.classRule;
    }

    public RewriteRule methodHandlesLookupRule() {
        return this.methodHandlesLookupRule;
    }

    public RewriteRule lambdaMetafactoryRule() {
        return this.lambdaMetafactoryRule;
    }

    public RewriteRule constantBootstrapsRule() {
        return this.constantBootstrapsRule;
    }

    public RewriteRule methodTypeRule() {
        return this.methodTypeRule;
    }

    public RewriteRule enumRule() {
        return this.enumRule;
    }

    public List<RewriteRule> builtInRules() {
        return List.of(
            this.classRule,
            this.methodHandlesLookupRule,
            this.lambdaMetafactoryRule,
            this.constantBootstrapsRule,
            this.methodTypeRule,
            this.enumRule
        );
    }

    private RewriteRule createClassRule() {
        return RewriteRule.methodVisitorBuilder(builder -> builder.visitBoth(InvokeStaticRewrite.forOwner(
            "java/lang/Class",
            (parent, owner, name, descriptor, isInterface) -> {
                if (((name.equals("getDeclaredField") || name.equals("getField")) && descriptor.equals("(Ljava/lang/String;)Ljava/lang/reflect/Field;"))
                    || ((name.equals("getDeclaredMethod") || name.equals("getMethod")) && descriptor.equals("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"))) {
                    final String redirectedDescriptor = "(Ljava/lang/Class;" + descriptor.substring(1);
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, redirectedDescriptor);
                } else if (name.equals("forName")) {
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, descriptor);
                }
                return null;
            }
        )));
    }

    private RewriteRule createMethodHandlesLookupRule() {
        return RewriteRule.methodVisitorBuilder(builder -> builder.visitBoth(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/MethodHandles$Lookup",
            (parent, owner, name, descriptor, isInterface) -> {
                if (((name.equals("findStatic") || name.equals("findVirtual")) && descriptor.equals("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"))
                    || (name.equals("findClass") && descriptor.equals("(Ljava/lang/String;)Ljava/lang/Class;"))
                    || (name.equals("findSpecial") && descriptor.equals("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;"))
                    || ((name.equals("findGetter") || name.equals("findSetter")) && descriptor.equals("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;"))) {
                    final String redirectedDescriptor = "(Ljava/lang/invoke/MethodHandles$Lookup;" + descriptor.substring(1);
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, redirectedDescriptor);
                }
                return null;
            }
        )));
    }

    private RewriteRule createLamdaMetafactoryRule() {
        return RewriteRule.methodVisitorBuilder(builder -> builder.visitBoth(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/LambdaMetafactory",
            (parent, owner, name, descriptor, isInterface) -> {
                if ((name.equals("metafactory") && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"))
                    || (name.equals("altMetafactory") && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"))) {
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, descriptor);
                }
                return null;
            }
        )));
    }

    private RewriteRule createConstantBootstrapsRule() {
        return RewriteRule.methodVisitorBuilder(builder -> builder.visitBoth(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/ConstantBootstraps",
            (parent, owner, name, descriptor, isInterface) -> {
                if ((name.equals("enumConstant") && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Enum;"))
                    || name.equals("getStaticFinal") && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/Object;")
                    || name.equals("getStaticFinal") && descriptor.equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;")) {
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, descriptor);
                }
                return null;
            }
        )));
    }

    private RewriteRule createMethodTypeRule() {
        return RewriteRule.methodVisitorBuilder(builder -> builder.visitBoth(InvokeStaticRewrite.forOwner(
            "java/lang/invoke/MethodType",
            (parent, owner, name, descriptor, isInterface) -> {
                if (name.equals("fromMethodDescriptorString") && descriptor.equals("(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;")) {
                    return InvokeStaticRewrite.staticRedirect(this.proxy, name, descriptor);
                }
                return null;
            }
        )));
    }

    private static final class EnumMethodVisitor extends MethodVisitor {
        private final String proxy;
        private final ClassInfoProvider classInfoProvider;
        private int increaseMaxStack;

        EnumMethodVisitor(final int api, final MethodVisitor parent, final String proxyClassName, final ClassInfoProvider classInfoProvider) {
            super(api, parent);
            this.proxy = proxyClassName;
            this.classInfoProvider = classInfoProvider;
        }

        @Override
        public void visitMaxs(final int maxStack, final int maxLocals) {
            super.visitMaxs(maxStack + this.increaseMaxStack, maxLocals);
            this.increaseMaxStack = 0;
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
            if ((owner.startsWith("net/minecraft/") || owner.startsWith("com/mojang/"))
                && name.equals("valueOf") && descriptor.equals("(Ljava/lang/String;)L" + owner + ";")) {
                // Rewrite SomeEnum.valueOf(String)
                final @Nullable ClassInfo info = this.classInfoProvider.info(owner);
                if (info != null && info.isEnum()) {
                    this.increaseMaxStack++; // Increase max stack size for this method by one for the class parameter
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

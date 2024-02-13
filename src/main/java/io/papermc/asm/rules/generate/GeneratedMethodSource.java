package io.papermc.asm.rules.generate;

import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import static io.papermc.asm.util.DescriptorUtils.toOwner;

public interface GeneratedMethodSource<C> extends GeneratedMethodHolder {

    @Override
    default void generateMethod(final Map.Entry<Executable, ? extends MethodTypeDesc> pair, final RewriteRule.MethodGeneratorFactory factory) {
        if (pair.getKey() instanceof final Method method) {
            this.generateRegularMethod(factory, method, pair.getValue());
        } else if (pair.getKey() instanceof final Constructor<?> constructor) {
            this.generateConstructor(factory, constructor, pair.getValue());
        } else {
            throw new IllegalStateException("Unknown executable " + pair.getKey());
        }
    }

    /**
     * Modifies the descriptor for the actual method for the generated method.
     *
     * @param existing the original (in source) method descriptor
     * @param context context for the generator
     * @return the modified descriptor
     */
    default MethodTypeDesc computeGeneratedDescriptor(final MethodTypeDesc existing, final C context) {
        return existing;
    }

    default void generateParameters(final GeneratorAdapter methodGenerator, final MethodTypeDesc descriptor, final C context) {
        for (int i = 0; i < descriptor.parameterCount(); i++) {
            methodGenerator.loadArg(i);
        }
    }

    private void generateConstructor(final RewriteRule.MethodGeneratorFactory factory, final Constructor<?> constructor, MethodTypeDesc descriptor) {
        final Class<?> declaringClass = constructor.getDeclaringClass();
        descriptor = descriptor.changeReturnType(constructor.getDeclaringClass().describeConstable().orElseThrow());
        final C context = this.createNewContext();
        descriptor = this.computeGeneratedDescriptor(descriptor, context);
        final String typeName = toOwner(descriptor.returnType());
        final GeneratorAdapter methodGenerator = factory.create(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "create" + typeName.substring(typeName.lastIndexOf('/') + 1), descriptor.descriptorString());
        methodGenerator.newInstance(Type.getType(declaringClass));
        methodGenerator.dup();
        this.generateParameters(methodGenerator, descriptor, context);
        methodGenerator.invokeConstructor(Type.getType(declaringClass), org.objectweb.asm.commons.Method.getMethod(constructor));
        this.generateReturnValue(methodGenerator, constructor);
        methodGenerator.endMethod();
    }

    private void generateRegularMethod(final RewriteRule.MethodGeneratorFactory factory, final Method method, MethodTypeDesc descriptor) {
        final Class<?> declaringClass = method.getDeclaringClass();
        if (!Modifier.isStatic(method.getModifiers())) { // if a non-static method, first param will be the owner type
            descriptor = descriptor.insertParameterTypes(0, declaringClass.describeConstable().orElseThrow());
        }
        final C context = this.createNewContext();
        descriptor = this.computeGeneratedDescriptor(descriptor, context);
        final GeneratorAdapter methodGenerator = factory.create(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, method.getName(), descriptor.descriptorString());
        this.generateParameters(methodGenerator, descriptor, context);
        final org.objectweb.asm.commons.Method originalMethod = org.objectweb.asm.commons.Method.getMethod(method);
        final Type originalOwner = Type.getType(declaringClass);
        if (declaringClass.isInterface() && !Modifier.isStatic(method.getModifiers())) {
            methodGenerator.invokeInterface(originalOwner, originalMethod);
        } else if (!declaringClass.isInterface() && !Modifier.isStatic(method.getModifiers())) {
            methodGenerator.invokeVirtual(originalOwner, originalMethod);
        } else if (Modifier.isStatic(method.getModifiers())) {
            methodGenerator.invokeStatic(originalOwner, originalMethod);
        } else {
            throw new IllegalStateException("unknown method type " + methodGenerator);
        }
        this.generateReturnValue(methodGenerator, method);
        methodGenerator.endMethod();
    }

    // @MustBeInvokedByOverriders
    default void generateReturnValue(final GeneratorAdapter methodGenerator, final Executable executable) {
        methodGenerator.returnValue();
    }

    C createNewContext();

    interface NoContext extends GeneratedMethodSource<Void> {

        @Override
        default Void createNewContext() {
            return null;
        }
    }
}

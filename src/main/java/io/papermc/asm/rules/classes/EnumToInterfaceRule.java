package io.papermc.asm.rules.classes;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import io.papermc.asm.rules.method.DirectStaticRewrite;
import io.papermc.asm.rules.method.OwnableMethodRewriteRule;
import io.papermc.asm.rules.method.generated.GeneratedStaticRewrite;
import io.papermc.asm.rules.method.rewrite.MethodRewrite;
import io.papermc.asm.rules.method.rewrite.SimpleRewrite;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import static io.papermc.asm.util.DescriptorUtils.desc;
import static io.papermc.asm.util.DescriptorUtils.replaceParameters;
import static io.papermc.asm.util.OpcodeUtils.isStatic;
import static java.util.function.Predicate.isEqual;

public class EnumToInterfaceRule implements RewriteRule.Delegate {

    private static final MethodMatcher ENUM_VIRTUAL_METHODS = MethodMatcher.builder()
        .match("name", b -> b.virtual().desc(MethodTypeDesc.of(ConstantDescs.CD_String)))
        .match("ordinal", b -> b.virtual().desc(MethodTypeDesc.of(ConstantDescs.CD_int)))
        .match("compareTo", b -> b.virtual().desc(MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_Enum)))
        .match("getDeclaringClass", b -> b.virtual().desc(MethodTypeDesc.of(ConstantDescs.CD_Class)))
        .match("describeConstable", b -> b.virtual().desc(MethodTypeDesc.of(desc(Optional.class))))
        .build();
    private static final MethodMatcher NOT_ENUM_METHODS_BASE = ENUM_VIRTUAL_METHODS.negate();

    private static final ClassDesc LEGACY_ENUM = desc(LegacyEnum.class);

    private final Map<ClassDesc, ClassDesc> enums;
    private final RewriteRule rule;
    private final MethodMatcher notEnumMethods;

    public EnumToInterfaceRule(final Map<ClassDesc, ClassDesc> enums) {
        this.enums = enums;
        final List<RewriteRule> rules = new ArrayList<>();
        rules.add(new EnumVirtualMethods());
        MethodMatcher notEnums = NOT_ENUM_METHODS_BASE;
        for (final Map.Entry<ClassDesc, ClassDesc> entry : enums.entrySet()) {
            final MethodMatcher matcher = createStaticMatcher(entry.getKey());
            notEnums = notEnums.and(matcher.negate());
            rules.add(new DirectStaticRewrite(Set.of(entry.getKey()), matcher, entry.getValue()));
        }
        rules.add(new NotEnumMethods());
        this.rule = RewriteRule.chain(rules);
        this.notEnumMethods = notEnums;
    }

    @Override
    public RewriteRule delegate() {
        return this.rule;
    }

    private static MethodMatcher createStaticMatcher(final ClassDesc legacyEnumType) {
        return MethodMatcher.builder()
            .match("values", b -> b.statik().desc(MethodTypeDesc.of(legacyEnumType.arrayType())))
            .match("valueOf", b -> b.statik().desc(MethodTypeDesc.of(legacyEnumType, ConstantDescs.CD_String)))
            .build();
    }

    final class EnumVirtualMethods implements GeneratedStaticRewrite, OwnableMethodRewriteRule.Filtered {

        @Override
        public MethodMatcher methodMatcher() {
            return ENUM_VIRTUAL_METHODS;
        }

        @Override
        public Set<ClassDesc> owners() {
            return EnumToInterfaceRule.this.enums.keySet();
        }

        @Override
        public MethodTypeDesc transformToRedirectDescriptor(final MethodTypeDesc intermediateDescriptor) {
            return replaceParameters(intermediateDescriptor, isEqual(ConstantDescs.CD_Enum), ConstantDescs.CD_Object);
        }

        @Override
        public void generateMethod(final GeneratorAdapterFactory factory, final MethodCallData modified, final MethodCallData original) {
            final GeneratorAdapter methodGenerator = this.createAdapter(factory, modified);
            final Type legacyEnumType = Type.getType(LEGACY_ENUM.descriptorString());
            for (int i = 0; i < modified.descriptor().parameterCount(); i++) {
                methodGenerator.loadArg(i);
                if (i == 0) {
                    // change type to LegacyEnum when first param (which for invokeinterface and invokevirtual is the owner object)
                    methodGenerator.checkCast(legacyEnumType);
                }
            }
            final MethodTypeDesc desc = replaceParameters(original.descriptor(), isEqual(ConstantDescs.CD_Enum), ConstantDescs.CD_Object);
            final Method newMethodType = new Method(original.name(), desc.descriptorString());
            methodGenerator.invokeInterface(legacyEnumType, newMethodType);
            methodGenerator.returnValue();
            methodGenerator.endMethod();
        }

        @Override
        public void generateConstructor(final GeneratorAdapterFactory factory, final MethodCallData modified, final ConstructorCallData original) {
            throw new UnsupportedOperationException("Doesn't work with constructors");
        }
    }

    final class NotEnumMethods implements OwnableMethodRewriteRule.Filtered {

        @Override
        public MethodMatcher methodMatcher() {
            return EnumToInterfaceRule.this.notEnumMethods;
        }

        @Override
        public Set<ClassDesc> owners() {
            return EnumToInterfaceRule.this.enums.keySet();
        }

        @Override
        public @Nullable MethodRewrite<?> rewrite(final ClassProcessingContext context, final boolean isInvokeDynamic, final int opcode, final ClassDesc owner, final String name, final MethodTypeDesc descriptor, final boolean isInterface) {
            if (isStatic(opcode, isInvokeDynamic)) {
                return new SimpleRewrite(opcode, owner, name, descriptor, true, isInvokeDynamic);
            } else {
                final int newOpcode;
                if (isInvokeDynamic && opcode == Opcodes.H_INVOKEVIRTUAL) {
                    newOpcode = Opcodes.H_INVOKEINTERFACE;
                } else if (!isInvokeDynamic && opcode == Opcodes.INVOKEVIRTUAL) {
                    newOpcode = Opcodes.INVOKEINTERFACE;
                } else {
                    return null; // just don't rewrite if nothing matches (maybe they already compiled against the interface)
                }
                return new SimpleRewrite(newOpcode, owner, name, descriptor, true, isInvokeDynamic);
            }
        }
    }
}

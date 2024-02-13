package io.papermc.asm.rules.builder.matcher;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Predicate;

public class TargetedMethodMatcherImpl extends MethodMatcherImpl implements TargetedMethodMatcher {

    private final ClassDesc oldType;

    TargetedMethodMatcherImpl(final Predicate<String> byName, final Predicate<MethodTypeDesc> byDesc, final ClassDesc oldType) {
        super(byName, (name, desc) -> byName.test(name) && byDesc.test(MethodTypeDesc.ofDescriptor(desc)));
        this.oldType = oldType;
    }

    @Override
    public ClassDesc targetType() {
        return this.oldType;
    }
}

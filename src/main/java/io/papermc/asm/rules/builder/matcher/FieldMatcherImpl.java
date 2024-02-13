package io.papermc.asm.rules.builder.matcher;

import java.lang.constant.ClassDesc;
import java.util.function.Predicate;

import static io.papermc.asm.util.DescriptorUtils.parseType;

public class FieldMatcherImpl implements FieldMatcher {

    private final Predicate<? super String> byName;
    private final Predicate<? super ClassDesc> byDesc;

    public FieldMatcherImpl(final Predicate<? super String> byName, final Predicate<? super ClassDesc> byDesc) {
        this.byName = byName;
        this.byDesc = byDesc;
    }

    @Override
    public boolean matchesName(final String name) {
        return this.byName.test(name);
    }

    @Override
    public boolean matches(final String name, final String descriptor) {
        return this.byName.test(name) && this.byDesc.test(parseType(descriptor));
    }
}

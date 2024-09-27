package io.papermc.asm.rules.rename;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static io.papermc.asm.util.DescriptorUtils.desc;
import static io.papermc.asm.util.DescriptorUtils.toOwner;

public final class RenameRuleBuilder implements io.papermc.asm.util.Builder<RenameRule> {

    RenameRuleBuilder() {
    }

    final Map<String, String> mappings = new HashMap<>();
    final Map<ClassDesc, EnumRenamer> enumValueOfFieldRenames = new HashMap<>();

    public RenameRuleBuilder methodByDesc(final Iterable<ClassDesc> owners, final String legacyMethodName, final MethodTypeDesc desc, final String newMethodName) {
        for (final ClassDesc owner : owners) {
            this.methodByDesc(owner, legacyMethodName, desc, newMethodName);
        }
        return this;
    }

    public RenameRuleBuilder methodByDesc(final ClassDesc owner, final String legacyMethodName, final MethodTypeDesc desc, final String newMethodName) {
        return this.methodByInternal(toOwner(owner), legacyMethodName, desc.descriptorString(), newMethodName);
    }

    public RenameRuleBuilder methodByInternal(final Iterable<String> owners, final String legacyMethodName, final MethodTypeDesc desc, final String newMethodName) {
        for (final String owner : owners) {
            this.methodByInternal(owner, legacyMethodName, desc.descriptorString(), newMethodName);
        }
        return this;
    }

    public RenameRuleBuilder methodByInternal(final String owner, final String legacyMethodName, final String desc, final String newMethodName) {
        this.mappings.put("%s.%s%s".formatted(owner, legacyMethodName, desc), newMethodName);
        return this;
    }

    public RenameRuleBuilder fieldByDesc(final Iterable<ClassDesc> owners, final String legacyFieldName, final String newFieldName) {
        for (final ClassDesc owner : owners) {
            this.fieldByDesc(owner, legacyFieldName, newFieldName);
        }
        return this;
    }

    public RenameRuleBuilder fieldByDesc(final ClassDesc owner, final String legacyFieldName, final String newFieldName) {
        return this.fieldByInternal(toOwner(owner), legacyFieldName, newFieldName);
    }

    public RenameRuleBuilder fieldByInternal(final Iterable<String> owners, final String legacyFieldName, final String newFieldName) {
        for (final String owner : owners) {
            this.fieldByInternal(owner, legacyFieldName, newFieldName);
        }
        return this;
    }

    public RenameRuleBuilder fieldByInternal(final String owner, final String legacyFieldName, final String newFieldName) {
        this.mappings.put("%s.%s".formatted(owner, legacyFieldName), newFieldName);
        return this;
    }

    /**
     * Note that you also have to remap the method for the annotation attribute.
     */
    public RenameRuleBuilder annotationAttribute(final ClassDesc owner, final String legacyName, final String newName) {
        return this.annotationAttribute(owner.descriptorString(), legacyName, newName);
    }

    /**
     * Note that you also have to remap the method for the annotation attribute.
     */
    public RenameRuleBuilder annotationAttribute(final String ownerDescriptor, final String legacyName, final String newName) {
        if (!ownerDescriptor.startsWith("L") || !ownerDescriptor.endsWith(";")) {
            throw new IllegalArgumentException("Invalid owner descriptor: %s".formatted(ownerDescriptor));
        }
        // for some reason the remapper wants the Lpkg/name; format, but just for annotation attributes
        this.mappings.put("%s.%s".formatted(ownerDescriptor, legacyName), newName);
        return this;
    }

    /**
     * Use {@code /} instead of {@code .}.
     */
    public RenameRuleBuilder type(final String legacyType, final ClassDesc newType) {
        this.mappings.put(legacyType, toOwner(newType));
        return this;
    }

    /**
     * Use {@code /} instead of {@code .}.
     */
    public RenameRuleBuilder type(final String legacyType, final String newType) {
        this.mappings.put(legacyType, newType);
        return this;
    }

    public RenameRuleBuilder editEnum(final Class<?> enumTypeDesc, final Consumer<EnumRenameBuilder> renamer) {
        return this.editEnum(desc(enumTypeDesc), renamer);
    }

    public RenameRuleBuilder editEnum(final ClassDesc enumTypeDesc, final Consumer<EnumRenameBuilder> renamer) {
        final EnumRenameBuilder enumRenamer = new EnumRenameBuilder(enumTypeDesc);
        renamer.accept(enumRenamer);
        enumRenamer.apply(this);
        return this;
    }

    @Override
    public RenameRule build() {
        return new RenameRule(this.mappings, this.enumValueOfFieldRenames);
    }
}

package io.papermc.asm.rules.rename;

import java.lang.constant.ClassDesc;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

import static io.papermc.asm.util.DescriptorUtils.desc;

public final class EnumRenameBuilder {

    private final ClassDesc enumTypeDesc;
    private @Nullable ClassDesc optionalEnumReplacementImpl;
    private final Map<String, String> enumFieldRenames = new HashMap<>();

    EnumRenameBuilder(final ClassDesc enumTypeDesc) {
        this.enumTypeDesc = enumTypeDesc;
    }

    public EnumRenameBuilder enumReplacementImpl(final Class<?> type) {
        return this.enumReplacementImpl(desc(type));
    }

    public EnumRenameBuilder enumReplacementImpl(final ClassDesc type) {
        if (this.enumTypeDesc.equals(type)) {
            throw new IllegalArgumentException("Cannot replace an enum with itself");
        }
        this.optionalEnumReplacementImpl = type;
        return this;
    }

    public EnumRenameBuilder rename(final String legacyName, final String newName) {
        this.enumFieldRenames.put(legacyName, newName);
        return this;
    }

    void apply(final RenameRuleBuilder renameRuleBuilder) {
        this.enumFieldRenames.forEach((legacyName, newName) -> {
            renameRuleBuilder.fieldByDesc(this.enumTypeDesc, legacyName, newName);
        });
        final Map<String, String> copy = Map.copyOf(this.enumFieldRenames);
        renameRuleBuilder.enumValueOfFieldRenames.put(this.enumTypeDesc, new EnumRenamer(this.enumTypeDesc, this.optionalEnumReplacementImpl, copy));
    }
}

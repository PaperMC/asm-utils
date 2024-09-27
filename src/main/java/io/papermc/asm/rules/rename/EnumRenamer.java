package io.papermc.asm.rules.rename;

import java.lang.constant.ClassDesc;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public record EnumRenamer(ClassDesc typeDesc, @Nullable ClassDesc optionalReplacementImpl, Map<String, String> fieldRenames) {
}

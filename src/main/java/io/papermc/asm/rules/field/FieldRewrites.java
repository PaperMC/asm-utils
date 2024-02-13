package io.papermc.asm.rules.field;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.builder.matcher.FieldMatcher;
import java.lang.constant.ClassDesc;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class FieldRewrites {

    private FieldRewrites() {
    }

    // Keep in mind that you have to include all subtype owners as well as the field could be referenced via them as well
    public record Rename(Set<Class<?>> owners, FieldMatcher fieldMatcher, String newName) implements FilteredFieldRewriteRule {

        @Override
        public @Nullable Rewrite rewrite(final ClassProcessingContext context, final int opcode, final String owner, final String name, final ClassDesc desc) {
            if (!name.equals(this.newName())) {
                return new RewriteField(opcode, owner, this.newName(), desc);
            }
            return null;
        }
    }
}

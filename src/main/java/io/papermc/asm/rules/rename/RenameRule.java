package io.papermc.asm.rules.rename;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

import static io.papermc.asm.util.DescriptorUtils.toOwner;

public final class RenameRule implements RewriteRule {

    public static Builder builder() {
        return new Builder();
    }

    private final Remapper remapper;

    public RenameRule(final Remapper remapper) {
        this.remapper = remapper;
    }

    @Override
    public ClassVisitor createVisitor(final int api, final ClassVisitor parent, final ClassProcessingContext context) {
        return new FixedClassRemapper(api, parent, this.remapper);
    }

    public static final class Builder implements io.papermc.asm.util.Builder<RenameRule> {

        private Builder() {
        }

        private final Map<String, String> mappings = new HashMap<>();

        public Builder methodByDesc(final Iterable<ClassDesc> owners, final String legacyMethodName, final MethodTypeDesc desc, final String newMethodName) {
            for (final ClassDesc owner : owners) {
                this.methodByDesc(owner, legacyMethodName, desc, newMethodName);
            }
            return this;
        }

        public Builder methodByDesc(final ClassDesc owner, final String legacyMethodName, final MethodTypeDesc desc, final String newMethodName) {
            return this.methodByInternal(toOwner(owner), legacyMethodName, desc.descriptorString(), newMethodName);
        }

        public Builder methodByInternal(final Iterable<String> owners, final String legacyMethodName, final MethodTypeDesc desc, final String newMethodName) {
            for (final String owner : owners) {
                this.methodByInternal(owner, legacyMethodName, desc.descriptorString(), newMethodName);
            }
            return this;
        }

        public Builder methodByInternal(final String owner, final String legacyMethodName, final String desc, final String newMethodName) {
            this.mappings.put("%s.%s%s".formatted(owner, legacyMethodName, desc), newMethodName);
            return this;
        }

        public Builder fieldsByDesc(final Iterable<ClassDesc> owners, final String legacyFieldName, final String newFieldName) {
            for (final ClassDesc owner : owners) {
                this.fieldByDesc(owner, legacyFieldName, newFieldName);
            }
            return this;
        }

        public Builder fieldByDesc(final ClassDesc owner, final String legacyFieldName, final String newFieldName) {
            return this.fieldByInternal(toOwner(owner), legacyFieldName, newFieldName);
        }

        public Builder fieldByInternal(final Iterable<String> owners, final String legacyFieldName, final String newFieldName) {
            for (final String owner : owners) {
                this.fieldByInternal(owner, legacyFieldName, newFieldName);
            }
            return this;
        }

        public Builder fieldByInternal(final String owner, final String legacyFieldName, final String newFieldName) {
            this.mappings.put("%s.%s".formatted(owner, legacyFieldName), newFieldName);
            return this;
        }

        /**
         * Note that you also have to remap the method for the annotation attribute.
         */
        public Builder annotationAttribute(final ClassDesc owner, final String legacyName, final String newName) {
            return this.annotationAttribute(owner.descriptorString(), legacyName, newName);
        }

        /**
         * Note that you also have to remap the method for the annotation attribute.
         */
        public Builder annotationAttribute(final String ownerDescriptor, final String legacyName, final String newName) {
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
        public Builder type(final String legacyType, final ClassDesc newType) {
            this.mappings.put(legacyType, toOwner(newType));
            return this;
        }

        /**
         * Use {@code /} instead of {@code .}.
         */
        public Builder type(final String legacyType, final String newType) {
            this.mappings.put(legacyType, newType);
            return this;
        }

        @Override
        public RenameRule build() {
            return new RenameRule(new SimpleRemapper(Map.copyOf(this.mappings)));
        }
    }
}

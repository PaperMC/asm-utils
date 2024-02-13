package io.papermc.asm.rules;

import io.papermc.asm.ClassProcessingContext;
import java.util.Set;
import java.util.function.Predicate;
import org.objectweb.asm.Type;

public interface OwnableRewriteRule extends RewriteRule {

    Set<Class<?>> owners();

    default boolean matchesOwner(final ClassProcessingContext context, final String owner) {
        return this.owners().stream().map(Type::getInternalName).anyMatch(Predicate.isEqual(owner));
    }
}

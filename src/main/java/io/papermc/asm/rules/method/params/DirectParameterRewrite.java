package io.papermc.asm.rules.method.params;

import io.papermc.asm.versioned.ApiVersion;
import io.papermc.asm.versioned.VersionedRuleFactory;
import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import io.papermc.asm.rules.method.generated.TargetedTypeGeneratedStaticRewrite;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Rewrites matching bytecode to a generated method which will invoke the static handler on all parameters that need to be converted. That
 * generated method then calls the original method with the new parameters.
 *
 * @param owners        the owners to target
 * @param existingType  the type to convert to
 * @param methodMatcher the method matcher to use which targets the legacy param type
 * @param staticHandler the method which will be used to convert the legacy type to the new type
 */
public record DirectParameterRewrite(Set<ClassDesc> owners, ClassDesc existingType, TargetedMethodMatcher methodMatcher, Method staticHandler) implements TargetedTypeGeneratedStaticRewrite.Parameter {

    public record Versioned(Set<ClassDesc> owners, ClassDesc existingType, NavigableMap<ApiVersion, Map.Entry<TargetedMethodMatcher, Method>> versions) implements VersionedRuleFactory {

        public Versioned {
            versions = Collections.unmodifiableNavigableMap(versions);
        }

        @Override
        public @Nullable RewriteRule createRule(final ApiVersion apiVersion) {
            final Map.@Nullable Entry<ApiVersion, Map.Entry<TargetedMethodMatcher, Method>> apiVersionEntryEntry = this.versions.ceilingEntry(apiVersion);
            if (apiVersionEntryEntry == null) {
                return null;
            }
            final Map.Entry<TargetedMethodMatcher, Method> entry = apiVersionEntryEntry.getValue();
            return new DirectParameterRewrite(this.owners, this.existingType, entry.getKey(), entry.getValue());
        }
    }
}

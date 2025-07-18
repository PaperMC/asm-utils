package io.papermc.asm.versioned;

import io.papermc.asm.rules.RewriteRule;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Caches creating {@link RewriteRule}s for each {@link ApiVersion}.
 */
public abstract class CachingVersionedRuleFactory implements VersionedRuleFactory {

    private final Map<ApiVersion<?>, RewriteRule> cache = new ConcurrentHashMap<>();
    private @Nullable VersionedRuleFactory rootFactory;

    @ApiStatus.OverrideOnly
    public abstract VersionedRuleFactory createRootFactory();

    protected final VersionedRuleFactory rootFactory() {
        if (this.rootFactory == null) {
            this.rootFactory = this.createRootFactory();
        }
        return this.rootFactory;
    }

    @Override
    public final RewriteRule createRule(final ApiVersion<?> apiVersion) {
        return this.cache.computeIfAbsent(apiVersion, this.rootFactory()::createRule);
    }
}

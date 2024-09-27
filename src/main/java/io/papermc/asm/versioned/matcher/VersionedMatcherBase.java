package io.papermc.asm.versioned.matcher;

import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.versioned.ApiVersion;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class VersionedMatcherBase<P> {

    private final NavigableMap<ApiVersion, P> map;

    protected VersionedMatcherBase(final NavigableMap<ApiVersion, P> map) {
        this.map = map;
    }

    public RewriteRule ruleForVersion(final ApiVersion version, final Function<P, ? extends RewriteRule> creator) {
        final Map.@Nullable Entry<ApiVersion, P> entry = this.map.ceilingEntry(version);
        if (entry == null) {
            return RewriteRule.EMPTY;
        }
        return creator.apply(entry.getValue());
    }
}

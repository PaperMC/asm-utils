package io.papermc.asm.versioned;

import io.papermc.asm.rules.RewriteRule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public record MappedVersionRuleFactory(NavigableMap<ApiVersion<?>, RewriteRule> versions) implements VersionedRuleFactory {

    public static MappedVersionRuleFactory create(final Map<? extends ApiVersion<?>, List<RewriteRule>> rules) {
        final Map<ApiVersion<?>, RewriteRule> map = new HashMap<>();
        rules.forEach((apiVersion, rewriteRules) -> {
            map.put(apiVersion, RewriteRule.chain(rewriteRules));
        });
        return new MappedVersionRuleFactory(new TreeMap<>(map));
    }

    @Override
    public RewriteRule createRule(final ApiVersion<?> apiVersion) {
        final List<RewriteRule> toMerge = new ArrayList<>(this.versions.tailMap(apiVersion, true).values());
        return RewriteRule.chain(toMerge);
    }
}

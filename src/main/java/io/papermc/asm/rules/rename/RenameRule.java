package io.papermc.asm.rules.rename;

import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.rename.asm.FixedClassRemapper;
import io.papermc.asm.versioned.Mergeable;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

public final class RenameRule implements RewriteRule.Delegate, Mergeable<RenameRule> {

    public static RenameRuleBuilder builder(final int api) {
        return new RenameRuleBuilderImpl(api);
    }

    private final int api;
    private final Map<String, String> renames;
    private final Map<ClassDesc, EnumRenamer> enumFieldRenames;
    private final Map<String, Map<String, List<PredicateMethodRemapper.MatcherPair>>> predicateMethodRemaps;
    private @Nullable RewriteRule rule;

    public RenameRule(final int api, final Map<String, String> renames, final Map<ClassDesc, EnumRenamer> enumFieldRenames, final Map<String, Map<String, List<PredicateMethodRemapper.MatcherPair>>> predicateMethodRemaps) {
        this.api = api;
        this.renames = Map.copyOf(renames);
        this.enumFieldRenames = Map.copyOf(enumFieldRenames);
        this.predicateMethodRemaps = Map.copyOf(predicateMethodRemaps);
    }

    public Map<String, String> renames() {
        return this.renames;
    }

    public Map<ClassDesc, EnumRenamer> enumFieldRenames() {
        return this.enumFieldRenames;
    }

    @Override
    public RewriteRule delegate() {
        if (this.rule == null) {
            final Remapper remapper = new SimpleRemapper(this.api, Map.copyOf(this.renames));
            final Remapper predicateRemapper = new PredicateMethodRemapper(this.api, this.predicateMethodRemaps);

            final List<RewriteRule> rules = new ArrayList<>(this.enumFieldRenames.size() + 1);
            this.enumFieldRenames.forEach((classDesc, enumRenamer) -> {
                rules.add(new EnumValueOfRewriteRule(enumRenamer));
            });
            rules.add((api, parent, context) -> new FixedClassRemapper(api, parent, predicateRemapper));
            rules.add((api, parent, context) -> new FixedClassRemapper(api, parent, remapper));

            this.rule = RewriteRule.chain(rules);
        }
        return this.rule;
    }

    @Override
    public RenameRule merge(final RenameRule other) {
        if (this.api != other.api) {
            throw new IllegalArgumentException("Cannot merge rules with different API versions");
        }
        final Map<String, String> regularRenames = new HashMap<>(this.renames);
        regularRenames.putAll(other.renames);

        final Map<ClassDesc, EnumRenamer> enumFieldRenames = new HashMap<>(this.enumFieldRenames);
        other.enumFieldRenames.forEach((cd, renamer) -> {
            enumFieldRenames.merge(cd, renamer, EnumRenamer::overwrite);
        });

        final Map<String, Map<String, List<PredicateMethodRemapper.MatcherPair>>> newPredicateRenames = new HashMap<>(this.predicateMethodRemaps);
        other.predicateMethodRemaps.forEach((s, methods) -> {
            final Map<String, List<PredicateMethodRemapper.MatcherPair>> newMethods = new HashMap<>(newPredicateRenames.computeIfAbsent(s, k -> new HashMap<>()));
            methods.forEach((name, pairs) -> {
                final List<PredicateMethodRemapper.MatcherPair> newPairs = new ArrayList<>(newMethods.computeIfAbsent(name, k -> new ArrayList<>()));
                newPairs.addAll(pairs);
                newMethods.put(name, List.copyOf(newPairs));
            });
            newPredicateRenames.put(s, Map.copyOf(newMethods));
        });

        return new RenameRule(this.api, regularRenames, enumFieldRenames, newPredicateRenames);
    }
}

package io.papermc.asm.rules.rename;

import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.rename.asm.FixedClassRemapper;
import io.papermc.asm.versioned.ApiVersion;
import io.papermc.asm.versioned.VersionedRuleFactory;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

public final class RenameRule implements RewriteRule.Delegate {

    public static RenameRuleBuilder builder() {
        return new RenameRuleBuilderImpl();
    }

    private final Map<String, String> renames;
    private final Map<ClassDesc, EnumRenamer> enumFieldRenames;
    private @MonotonicNonNull RewriteRule rule;

    public RenameRule(final Map<String, String> renames, final Map<ClassDesc, EnumRenamer> enumFieldRenames) {
        this.renames = Map.copyOf(renames);
        this.enumFieldRenames = Map.copyOf(enumFieldRenames);
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
            final Remapper remapper = new SimpleRemapper(Map.copyOf(this.renames));

            final List<RewriteRule> rules = new ArrayList<>(this.enumFieldRenames.size() + 1);
            this.enumFieldRenames.forEach((classDesc, enumRenamer) -> {
                rules.add(new EnumValueOfRewriteRule(enumRenamer));
            });
            rules.add((api, parent, context) -> new FixedClassRemapper(api, parent, remapper));

            this.rule = RewriteRule.chain(rules);
        }
        return this.rule;
    }

    public record Versioned(NavigableMap<ApiVersion, RenameRule> versions) implements VersionedRuleFactory {

        @Override
        public RewriteRule createRule(final ApiVersion apiVersion) {
            final List<RenameRule> toMerge = new ArrayList<>(this.versions.tailMap(apiVersion, true).values());
            if (toMerge.isEmpty()) {
                return RewriteRule.EMPTY;
            } else if (toMerge.size() == 1) {
                return toMerge.get(0);
            }
            Collections.reverse(toMerge);
            final Map<String, String> regularRenames = new HashMap<>();
            final Map<ClassDesc, EnumRenamer> enumFieldRenames = new HashMap<>();
            for (final RenameRule renameRule : toMerge) {
                regularRenames.putAll(renameRule.renames);
                renameRule.enumFieldRenames.forEach((classDesc, renamer) -> {
                    enumFieldRenames.merge(classDesc, renamer, EnumRenamer::overwrite);
                });
            }
            return new RenameRule(regularRenames, enumFieldRenames);
        }
    }
}

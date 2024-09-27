package io.papermc.asm.rules.rename;

import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.rename.asm.FixedClassRemapper;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

public final class RenameRule implements RewriteRule.Delegate {

    private final RewriteRule rule;

    public RenameRule(final Map<String, String> renames, final Map<ClassDesc, EnumRenamer> enumValueOfFieldRenames) {
        final Remapper remapper = new SimpleRemapper(Map.copyOf(renames));

        final List<RewriteRule> rules = new ArrayList<>(enumValueOfFieldRenames.size() + 1);
        enumValueOfFieldRenames.forEach((classDesc, enumRenamer) -> {
            rules.add(new EnumValueOfRewriteRule(enumRenamer));
        });
        rules.add((api, parent, context) -> new FixedClassRemapper(api, parent, remapper));

        this.rule = RewriteRule.chain(rules);
    }

    public static RenameRuleBuilder builder() {
        return new RenameRuleBuilder();
    }

    @Override
    public RewriteRule delegate() {
        return this.rule;
    }
}

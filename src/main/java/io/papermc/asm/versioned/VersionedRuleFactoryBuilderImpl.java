package io.papermc.asm.versioned;

import io.papermc.asm.rules.builder.matcher.method.targeted.TargetedMethodMatcher;
import io.papermc.asm.rules.method.params.DirectParameterRewrite;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

public class VersionedRuleFactoryBuilderImpl implements VersionedRuleFactoryBuilder {

    final Set<ClassDesc> owners;
    private final List<VersionedRuleFactory> factories = new ArrayList<>();

    public VersionedRuleFactoryBuilderImpl(final Set<ClassDesc> owners) {
        this.owners = Set.copyOf(owners);
    }

    @Override
    public void changeParamDirect(final ClassDesc newParamType, final NavigableMap<ApiVersion, Map.Entry<TargetedMethodMatcher, Method>> versions) {
        this.factories.add(new DirectParameterRewrite.Versioned(this.owners, newParamType, versions));
    }

    @Override
    public VersionedRuleFactory build() {
        if (this.factories.size() == 1) {
            return this.factories.get(0);
        }
        return VersionedRuleFactory.chain(this.factories);
    }
}

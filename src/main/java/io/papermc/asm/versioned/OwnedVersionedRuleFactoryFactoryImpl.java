package io.papermc.asm.versioned;

import io.papermc.asm.rules.method.params.DirectParameterRewrite;
import io.papermc.asm.rules.method.params.FuzzyParameterRewrite;
import io.papermc.asm.rules.method.params.SuperTypeParamRewrite;
import io.papermc.asm.rules.method.returns.SubTypeReturnRewrite;
import io.papermc.asm.versioned.matcher.VersionedMethodMatcher;
import io.papermc.asm.versioned.matcher.targeted.VersionedTargetedMethodMatcher;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OwnedVersionedRuleFactoryFactoryImpl implements OwnedVersionedRuleFactoryFactory {

    final Set<ClassDesc> owners;
    private final List<VersionedRuleFactory> factories = new ArrayList<>();

    public OwnedVersionedRuleFactoryFactoryImpl(final Set<ClassDesc> owners) {
        this.owners = Set.copyOf(owners);
    }

    @Override
    public void changeParamToSuper(final ClassDesc newParamType, final VersionedMethodMatcher versions) {
        this.factories.add(new SuperTypeParamRewrite.Versioned(this.owners, newParamType, versions));
    }

    @Override
    public void changeParamFuzzy(final ClassDesc newParamType, final VersionedTargetedMethodMatcher versions) {
        this.factories.add(new FuzzyParameterRewrite.Versioned(this.owners, newParamType, versions));
    }

    @Override
    public void changeParamDirect(final ClassDesc newParamType, final VersionedTargetedMethodMatcher versions) {
        this.factories.add(new DirectParameterRewrite.Versioned(this.owners, newParamType, versions));
    }

    @Override
    public void changeReturnTypeToSub(final ClassDesc newReturnType, final VersionedMethodMatcher versions) {
        this.factories.add(new SubTypeReturnRewrite.Versioned(this.owners, newReturnType, versions));
    }

    @Override
    public void addRuleFactory(final VersionedRuleFactory factory) {
        this.factories.add(factory);
    }

    @Override
    public VersionedRuleFactory build() {
        return VersionedRuleFactory.chain(this.factories);
    }
}

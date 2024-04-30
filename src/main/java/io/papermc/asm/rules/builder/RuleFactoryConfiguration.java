package io.papermc.asm.rules.builder;

import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.ClassDesc;
import java.util.Collections;
import java.util.Set;

public interface RuleFactoryConfiguration {

    static RuleFactoryConfiguration create(final ClassDesc delegateOwner) {
        return new RuleFactoryConfigurationImpl(delegateOwner);
    }

    ClassDesc delegateOwner();

    interface Holder {

        RuleFactoryConfiguration configuration();

        default RewriteRule forOwner(final Class<?> owner, final ConfiguredRuleFactory.Factory firstFactoryConsumer, final ConfiguredRuleFactory.Factory ...factoryConsumers) {
            return this.forOwners(Collections.singleton(owner), firstFactoryConsumer, factoryConsumers);
        }

        default RewriteRule forOwners(final Set<Class<?>> owners, final ConfiguredRuleFactory.Factory firstFactoryConsumer, final ConfiguredRuleFactory.Factory ...factoryConsumers) {
            final ConfiguredRuleFactory factory = ConfiguredRuleFactory.create(owners, this.configuration());
            firstFactoryConsumer.accept(factory);
            for (final ConfiguredRuleFactory.Factory factoryConsumer : factoryConsumers) {
                factoryConsumer.accept(factory);
            }
            return factory.build();
        }
    }
}

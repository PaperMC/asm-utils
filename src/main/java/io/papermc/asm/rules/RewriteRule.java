package io.papermc.asm.rules;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.builder.RuleFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;

public interface RewriteRule {

    RewriteRule EMPTY = (api, parent, context) -> new ClassVisitor(api, parent) {};

    @SafeVarargs
    static RewriteRule forOwner(final Class<?> owner, final Consumer<? super RuleFactory> firstFactoryConsumer, final Consumer<? super RuleFactory> ...factoryConsumers) {
        return forOwners(Collections.singleton(owner), firstFactoryConsumer, factoryConsumers);
    }

    @SafeVarargs
    static RewriteRule forOwners(final Set<Class<?>> owners, final Consumer<? super RuleFactory> firstFactoryConsumer, final Consumer<? super RuleFactory> ...factoryConsumers) {
        final RuleFactory factory = RuleFactory.create(owners);
        firstFactoryConsumer.accept(factory);
        for (final Consumer<? super RuleFactory> factoryConsumer : factoryConsumers) {
            factoryConsumer.accept(factory);
        }
        return factory.build();
    }

    static RewriteRule chain(final RewriteRule... rules) {
        return chain(Arrays.asList(rules));
    }

    static RewriteRule chain(final List<? extends RewriteRule> rules) {
        return new Chain(List.copyOf(rules));
    }

    ClassVisitor createVisitor(int api, ClassVisitor parent, final ClassProcessingContext context);

    default void generateMethods(final MethodGeneratorFactory methodGeneratorFactory) {
    }

    @FunctionalInterface
    interface MethodGeneratorFactory {
        GeneratorAdapter create(int access, String name, String descriptor);
    }

    final class Chain implements RewriteRule {

        private final List<RewriteRule> rules;

        private Chain(final List<RewriteRule> rules) {
            this.rules = rules;
        }

        @Override
        public ClassVisitor createVisitor(final int api, final ClassVisitor parent, final ClassProcessingContext context) {
            ClassVisitor visitor = parent;
            for (final RewriteRule rule : this.rules) {
                visitor = rule.createVisitor(api, visitor, context);
            }
            return visitor;
        }

        @Override
        public void generateMethods(final MethodGeneratorFactory methodGeneratorFactory) {
            this.rules.forEach(rule -> rule.generateMethods(methodGeneratorFactory));
        }
    }
}

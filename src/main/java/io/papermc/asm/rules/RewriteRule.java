package io.papermc.asm.rules;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.builder.RuleFactory;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;

@FunctionalInterface
public interface RewriteRule {

    RewriteRule EMPTY = (api, parent, context) -> new ClassVisitor(api, parent) {};

    @SafeVarargs
    static RewriteRule forOwnerClass(final Class<?> owner, final Consumer<? super RuleFactory> firstFactoryConsumer, final Consumer<? super RuleFactory>... factoryConsumers) {
        return forOwnerClasses(Collections.singleton(owner), firstFactoryConsumer, factoryConsumers);
    }

    @SafeVarargs
    static RewriteRule forOwnerClasses(final Set<Class<?>> owners, final Consumer<? super RuleFactory> firstFactoryConsumer, final Consumer<? super RuleFactory>... factoryConsumers) {
        return forOwners(owners.stream().map(c -> c.describeConstable().orElseThrow()).collect(Collectors.toUnmodifiableSet()), firstFactoryConsumer, factoryConsumers);
    }

    @SafeVarargs
    static RewriteRule forOwner(final ClassDesc owner, final Consumer<? super RuleFactory> firstFactoryConsumer, final Consumer<? super RuleFactory>... factoryConsumers) {
        return forOwners(Collections.singleton(owner), firstFactoryConsumer, factoryConsumers);
    }

    @SafeVarargs
    static RewriteRule forOwners(final Set<ClassDesc> owners, final Consumer<? super RuleFactory> firstFactoryConsumer, final Consumer<? super RuleFactory>... factoryConsumers) {
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

    static RewriteRule chain(final Collection<? extends RewriteRule> rules) {
        return new Chain(List.copyOf(rules));
    }

    static ChainBuilder chain() {
        return new ChainBuilder();
    }

    ClassVisitor createVisitor(int api, ClassVisitor parent, ClassProcessingContext context);

    @FunctionalInterface
    interface GeneratorAdapterFactory {
        GeneratorAdapter create(int access, String name, String descriptor);
    }

    interface Delegate extends RewriteRule {

        RewriteRule delegate();

        @Override
        default ClassVisitor createVisitor(final int api, final ClassVisitor parent, final ClassProcessingContext context) {
            return this.delegate().createVisitor(api, parent, context);
        }
    }

    record Chain(List<RewriteRule> rules) implements RewriteRule {
        public Chain(final List<RewriteRule> rules) {
            this.rules = List.copyOf(rules);
        }

        @Override
        public ClassVisitor createVisitor(final int api, final ClassVisitor parent, final ClassProcessingContext context) {
            ClassVisitor visitor = parent;
            for (final RewriteRule rule : this.rules) {
                visitor = rule.createVisitor(api, visitor, context);
            }
            return visitor;
        }
    }

    final class ChainBuilder {
        private final List<RewriteRule> rules = new ArrayList<>();

        private ChainBuilder() {
        }

        public ChainBuilder then(final RewriteRule rule) {
            this.rules.add(rule);
            return this;
        }

        public ChainBuilder then(final Collection<? extends RewriteRule> rules) {
            this.rules.addAll(rules);
            return this;
        }

        public ChainBuilder then(final RewriteRule... rules) {
            return this.then(Arrays.asList(rules));
        }

        public RewriteRule build() {
            return new Chain(this.rules);
        }
    }
}

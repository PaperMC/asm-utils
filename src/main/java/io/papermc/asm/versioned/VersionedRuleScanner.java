package io.papermc.asm.versioned;

import io.papermc.asm.rules.RewriteRule;
import io.papermc.asm.rules.RuleScanner;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class VersionedRuleScanner {

    private final Function<String, ApiVersion<?>> versionFactory;

    public VersionedRuleScanner(final Function<String, ApiVersion<?>> versionFactory) {
        this.versionFactory = versionFactory;
    }

    public VersionedRuleFactory scan(final Collection<Class<?>> classes) {
        return VersionedRuleFactory.chain(classes.stream().map(this::scan).toList());
    }

    public VersionedRuleFactory scan(final Class<?> clazz) {
        final Map<ApiVersion<?>, List<RewriteRule>> versions = new HashMap<>();

        methods: for (final Method method : clazz.getDeclaredMethods()) {
            if (RuleScanner.isNotValidMethod(method)) {
                continue;
            }
            // only public static methods
            for (final Map.Entry<Class<? extends Annotation>, RuleScanner.AnnotatedMethodFactory<?, ?>> entry : RuleScanner.FACTORIES.entrySet()) {
                final Class<? extends Annotation> annotation = entry.getKey();
                final RuleScanner.AnnotatedMethodFactory<?, ?> factory = entry.getValue();
                if (method.isAnnotationPresent(annotation)) {
                    if (!method.isAnnotationPresent(Version.class)) {
                        throw new IllegalArgumentException("Method " + method + " annotated with " + annotation + " is not annotated with @Version");
                    }
                    //noinspection DataFlowIssue
                    final ApiVersion<?> version = this.versionFactory.apply(method.getAnnotation(Version.class).value());
                    versions.computeIfAbsent(version, k -> new ArrayList<>()).add(RuleScanner.buildRule(method, annotation, factory));
                    continue methods;
                }
            }
        }

        return MappedVersionRuleFactory.create(versions);
    }

}

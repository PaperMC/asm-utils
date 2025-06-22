package io.papermc.asm.rules;

import io.papermc.asm.rules.method.DirectStaticRewrite;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus;

public final class RuleScanner {

    private static final Map<Class<? extends Annotation>, AnnotatedMethodFactory<?, ?>> INTERNAL_FACTORIES = new HashMap<>();

    static {
        register(DirectStaticRewrite.Wrapper.class, DirectStaticRewrite::create);
    }

    public static final Map<Class<? extends Annotation>, AnnotatedMethodFactory<?, ?>> FACTORIES = Collections.unmodifiableMap(INTERNAL_FACTORIES);

    private static <A extends Annotation> void register(final Class<A> annotationClass, final AnnotatedMethodFactory<A, ? extends RewriteRule> factory) {
        if (INTERNAL_FACTORIES.containsKey(annotationClass)) {
            throw new IllegalArgumentException("Factory for " + annotationClass.getName() + " is already registered");
        }
        INTERNAL_FACTORIES.put(annotationClass, factory);
    }

    public static RewriteRule scan(final Collection<Class<?>> classes) {
        return RewriteRule.chain(classes.stream().map(RuleScanner::scan).toList());
    }

    public static RewriteRule scan(final Class<?> clazz) {
        final List<RewriteRule> rules = new ArrayList<>();
        methods: for (final Method method : clazz.getDeclaredMethods()) {
            if (isNotValidMethod(method)) {
                continue;
            }
            // only public static methods
            for (final Map.Entry<Class<? extends Annotation>, AnnotatedMethodFactory<?, ?>> entry : FACTORIES.entrySet()) {
                final Class<? extends Annotation> annotation = entry.getKey();
                final AnnotatedMethodFactory<?, ?> factory = entry.getValue();
                if (method.isAnnotationPresent(annotation)) {
                    rules.add(buildRule(method, annotation, factory));
                    continue methods;
                }
            }
        }

        return RewriteRule.chain(rules);
    }

    public static boolean isNotValidMethod(final Method method) {
        if (method.isBridge() || method.isSynthetic()) {
            return true;
        }
        return !Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers());
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public static <A extends Annotation> RewriteRule buildRule(final Method method, final Class<A> annotationClass, final AnnotatedMethodFactory<?, ?> factory) {
        final A instance = Objects.requireNonNull(method.getAnnotation(annotationClass));
        return ((AnnotatedMethodFactory<A, ?>) factory).create(method, instance);
    }

    public interface AnnotatedMethodFactory<A extends Annotation, R extends RewriteRule> {

        R create(Method method, A annotation);
    }

    private RuleScanner() {
    }
}

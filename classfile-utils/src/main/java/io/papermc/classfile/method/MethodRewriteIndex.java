package io.papermc.classfile.method;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MethodRewriteIndex {

    private final Map<ClassDesc, NameIndex> index;
    private final Map<ClassDesc, List<MethodRewrite>> ctorIndex;

    public MethodRewriteIndex(final List<MethodRewrite> rewrites) {
        final Map<ClassDesc, NameIndex> mutable = new HashMap<>();
        final Map<ClassDesc, List<MethodRewrite>> ctorIndex = new HashMap<>();
        for (final MethodRewrite rewrite : rewrites) {
            if (rewrite.methodName() instanceof MethodNamePredicate.Constructor) {
                final List<MethodRewrite> existingRewrites = ctorIndex.computeIfAbsent(rewrite.owner(), $ -> new ArrayList<>());
                existingRewrites.add(rewrite);
                // TODO check that we can still add them to the index
                // continue;
            }
            final NameIndex nameIndex = mutable.computeIfAbsent(rewrite.owner(), _ -> new NameIndex());
            final List<String> exactNames = exactNames(rewrite.methodName());
            if (!exactNames.isEmpty()) {
                exactNames.forEach(name -> nameIndex.add(rewrite, name));
            } else {
                nameIndex.addWildcard(rewrite);
            }
        }
        this.ctorIndex = ctorIndex.entrySet().stream().collect(Collectors.toUnmodifiableMap(
            Map.Entry::getKey,
            e -> List.copyOf(e.getValue())
        ));
        this.index = mutable.entrySet().stream().collect(Collectors.toUnmodifiableMap(
            Map.Entry::getKey,
            e -> e.getValue().toImmutable()
        ));
    }

    private static List<String> exactNames(final MethodNamePredicate predicate) {
        if (!(predicate instanceof MethodNamePredicate.ExactMatch(final List<String> names))) {
            return Collections.emptyList();
        }
        return names;
    }

    public boolean hasConstructorRewrites() {
        return !this.ctorIndex.isEmpty();
    }

    public List<MethodRewrite> constructorCandidates(final ClassDesc owner) {
        final List<MethodRewrite> rewrites = this.ctorIndex.get(owner);
        if (rewrites == null) {
            return Collections.emptyList();
        }
        return rewrites;
    }

    public List<MethodRewrite> candidates(final ClassDesc owner, final String methodName) {
        final NameIndex nameIndex = this.index.get(owner);
        if (nameIndex == null) {
            return Collections.emptyList();
        }
        return nameIndex.candidates(methodName);
    }

    private record NameIndex(Map<String, List<MethodRewrite>> exact, List<MethodRewrite> wildcards) {

        NameIndex() {
            this(new HashMap<>(), new ArrayList<>());
        }

        void add(final MethodRewrite rewrite, final String exactName) {
            this.exact.computeIfAbsent(exactName, _ -> new ArrayList<>()).add(rewrite);
        }

        void addWildcard(final MethodRewrite rewrite) {
            this.wildcards.add(rewrite);
        }

        List<MethodRewrite> candidates(final String methodName) {
            final List<MethodRewrite> exact = this.exact.getOrDefault(methodName, List.of());
            if (this.wildcards.isEmpty()) {
                return exact;
            }
            if (exact.isEmpty()) {
                return this.wildcards;
            }
            final List<MethodRewrite> combined = new ArrayList<>(exact.size() + this.wildcards.size());
            combined.addAll(exact);
            combined.addAll(this.wildcards);
            return combined;
        }

        NameIndex toImmutable() {
            return new NameIndex(
                this.exact.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> List.copyOf(e.getValue()))),
                List.copyOf(this.wildcards)
            );
        }
    }
}

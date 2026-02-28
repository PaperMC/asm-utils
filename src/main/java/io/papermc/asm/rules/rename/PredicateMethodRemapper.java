package io.papermc.asm.rules.rename;

import io.papermc.asm.rules.builder.matcher.method.MethodMatcher;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.objectweb.asm.commons.Remapper;

public class PredicateMethodRemapper extends Remapper {

    private final Map<String, Map<String, List<MatcherPair>>> methodRemaps;

    public PredicateMethodRemapper(final int api, final Map<String, Map<String, List<MatcherPair>>> methodRemaps) {
        super(api);
        this.methodRemaps = methodRemaps;
    }

    @Override
    public String mapMethodName(final String owner, final String name, final String descriptor) {
        final Map<String, List<MatcherPair>> ownerRemaps = this.methodRemaps.get(owner);
        if (ownerRemaps != null) {
            final List<MatcherPair> methodRemaps = ownerRemaps.get(name);
            if (methodRemaps != null) {
                for (final MatcherPair remap : methodRemaps) {
                    if (remap.matcher().matches(name, descriptor)) {
                        return remap.newName.apply(name);
                    }
                }
            }
        }
        return super.mapMethodName(owner, name, descriptor);
    }

    public record MatcherPair(MethodMatcher matcher, UnaryOperator<String> newName) {}
}

package io.papermc.asm.rules.builder.matcher.method;

import io.papermc.asm.util.Builder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface MethodMatcherBuilder extends Builder<MethodMatcher> {

    MethodMatcherBuilder ctor(final Consumer<MatchBuilder> matchBuilderConsumer);

    MethodMatcherBuilder match(final String name, final Consumer<MatchBuilder> matchBuilderConsumer);

    MethodMatcherBuilder match(final Collection<String> names, final Consumer<MatchBuilder> matchBuilderConsumer);

    MethodMatcherBuilder hasParam(final ClassDesc paramClassDesc);

    MethodMatcherBuilder hasReturn(final ClassDesc returnClassDesc);

    /**
     * Used to match methods with specific names.
     *
     * @see MethodMatcherBuilder#match(String, Consumer)
     */
    interface MatchBuilder {

        MatchBuilder virtual();

        MatchBuilder statik();

        MatchBuilder type(final MethodType... types);

        MatchBuilder hasParam(final ClassDesc paramClassDesc);

        MatchBuilder hasReturn(final ClassDesc returnClassDesc);

        MatchBuilder desc(final String... descriptors);

        MatchBuilder desc(final MethodTypeDesc... descriptors);

        MatchBuilder desc(final Predicate<? super MethodTypeDesc> descPredicate);
    }
}

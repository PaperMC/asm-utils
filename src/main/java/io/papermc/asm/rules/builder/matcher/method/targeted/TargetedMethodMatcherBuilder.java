package io.papermc.asm.rules.builder.matcher.method.targeted;

import io.papermc.asm.rules.builder.matcher.method.MethodType;
import io.papermc.asm.util.Builder;
import java.lang.constant.ClassDesc;
import java.util.Collection;
import java.util.function.Consumer;

public interface TargetedMethodMatcherBuilder extends Builder<TargetedMethodMatcher> {

    TargetedMethodMatcherBuilder ctor();

    TargetedMethodMatcherBuilder match(final String name, final Consumer<MatchBuilder> matchBuilderConsumer);

    TargetedMethodMatcherBuilder match(final Collection<String> names, final Consumer<MatchBuilder> matchBuilderConsumer);

    TargetedMethodMatcherBuilder hasParam(final ClassDesc paramClassDesc);

    TargetedMethodMatcherBuilder hasReturn(final ClassDesc returnClassDesc);

    interface MatchBuilder {

        MatchBuilder virtual();

        MatchBuilder statik();

        MatchBuilder type(final MethodType... types);
    }
}

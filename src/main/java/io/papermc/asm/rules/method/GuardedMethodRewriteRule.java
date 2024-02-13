package io.papermc.asm.rules.method;

import io.papermc.asm.ClassProcessingContext;
import io.papermc.asm.rules.OwnableRewriteRule;
import io.papermc.asm.rules.builder.matcher.MethodMatcher;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static io.papermc.asm.util.DescriptorUtils.fromExecutable;

/**
 * A rule that targets specific methods and owners.
 */
public interface GuardedMethodRewriteRule extends MethodRewriteRule, OwnableRewriteRule {

    private static String transformExecutableName(final Executable executable) {
        return executable instanceof Constructor<?> ? "<init>" : executable.getName();
    }

    MethodMatcher methodMatcher();

    @Override
    default boolean shouldProcess(final ClassProcessingContext context, final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        return this.matchesOwner(context, owner) && this.methodMatcher().matches(name, descriptor);
    }

    default Stream<Map.Entry<Executable, MethodTypeDesc>> matchingMethodsByName() {
        return this.owners().stream()
            .flatMap(o -> Stream.concat(Arrays.stream(o.getDeclaredMethods()), Arrays.stream(o.getDeclaredConstructors())))
            .filter(executable -> Modifier.isPublic(executable.getModifiers())) // we only care about public stuff since that is API
            .map(executable -> Map.entry(executable, fromExecutable(executable)))
            .filter(pair -> this.methodMatcher().matchesName(transformExecutableName(pair.getKey())));
    }
}

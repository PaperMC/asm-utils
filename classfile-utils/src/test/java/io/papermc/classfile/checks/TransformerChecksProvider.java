package io.papermc.classfile.checks;

import io.papermc.classfile.TransformerTest;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.junit.platform.commons.support.AnnotationSupport;

public class TransformerChecksProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
        final TransformerTest test = AnnotationSupport.findAnnotation(context.getTestMethod(), TransformerTest.class).orElseThrow();
        return Stream.of(
            Arguments.of(new RewriteTransformerCheck(test.value())),
            Arguments.of(new ExecutionTransformerCheck(test.value()))
        );
    }
}

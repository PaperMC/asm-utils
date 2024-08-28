package io.papermc.asm;

import io.papermc.asm.checks.TransformerChecksProvider;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ParameterizedTest(name = "{arguments}")
@ArgumentsSource(TransformerChecksProvider.class)
public @interface TransformerTest {
    @Language("jvm-class-name")
    String value();

    boolean copyFromClassReader() default true;
}

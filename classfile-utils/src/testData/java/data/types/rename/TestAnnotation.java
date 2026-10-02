package data.types.rename;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestAnnotation {

    TestEnum single();

    TestEnum[] multiple() default {};

    Class<?> clazz() default void.class;
}

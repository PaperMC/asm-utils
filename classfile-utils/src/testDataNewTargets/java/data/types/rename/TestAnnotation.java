package data.types.rename;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestAnnotation {

    RenamedTestEnum value();

    RenamedTestEnum[] multiple() default {};

    Class<?> clazz() default void.class;
}

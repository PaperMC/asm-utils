package data.classes;

import data.types.classes.SomeAbstractClass;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class ClassToInterfaceRedirectUser extends SomeAbstractClass {

    public static void entry() {
        final SomeAbstractClass someAbstractClass = new ClassToInterfaceRedirectUser();
        someAbstractClass.doSomething();
        final String name = someAbstractClass.getName();

        final String staticString = SomeAbstractClass.getStaticString();

        final Consumer<SomeAbstractClass> doSomething = SomeAbstractClass::doSomething;
        doSomething.accept(someAbstractClass);

        final Supplier<String> getName = someAbstractClass::getName;
        final String name2 = getName.get();

        final Function<SomeAbstractClass, String> getName2 = SomeAbstractClass::getName;
        final String name3 = getName2.apply(someAbstractClass);

        final Supplier<String> getStaticString = SomeAbstractClass::getStaticString;
        final String staticString2 = getStaticString.get();
    }

    @Override
    public void doSomething() {
    }
}

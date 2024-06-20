package data.classes;

import data.types.classes.SomeAbstractClass;

@SuppressWarnings("unused")
public final class ClassToInterfaceRedirectUser extends SomeAbstractClass {

    public static void entry() {
        SomeAbstractClass someAbstractClass = new ClassToInterfaceRedirectUser();
        someAbstractClass.doSomething();
        String name = someAbstractClass.getName();

        final String staticString = SomeAbstractClass.getStaticString();
    }

    @Override
    public void doSomething() {
    }
}

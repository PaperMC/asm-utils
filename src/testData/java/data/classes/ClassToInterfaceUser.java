package data.classes;

import data.types.classes.SomeAbstractClass;
import data.types.classes.SomeAbstractClassImpl;

@SuppressWarnings("unused")
public final class ClassToInterfaceUser {

    public static void entry() {
        SomeAbstractClass someAbstractClass = new SomeAbstractClassImpl();
        someAbstractClass.doSomething();
        String name = someAbstractClass.getName();

        final String staticString = SomeAbstractClass.getStaticString();
    }
}

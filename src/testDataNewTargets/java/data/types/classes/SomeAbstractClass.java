package data.types.classes;

public interface SomeAbstractClass {

    static String getStaticString() {
        return "test";
    }

    String getName();

    void doSomething();
}

package data;

public class SameClassTargetUser {
    public static void entry() {
        consume(SameClassTarget.A);
    }

    static void consume(final Object o) {
    }
}

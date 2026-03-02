package data.types.apiimpl;

public enum ApiEnum {
    A("A"),
    B("B"),
    C("C");

    private final String key;

    ApiEnum(final String key) {
        this.key = key;
    }

    public static String getKeyStatic() {
        return "testStatic";
    }

    public String getKey() {
        return this.key;
    }
}

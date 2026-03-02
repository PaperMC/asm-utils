package data.types.apiimpl;

public interface ApiEnum {

    static String getKeyStatic() {
        return "testStatic";
    }

    ApiEnum A = new ApiEnumImpl("A");
    ApiEnum B = new ApiEnumImpl("B");
    ApiEnum C = new ApiEnumImpl("C");

    String getKey();
}

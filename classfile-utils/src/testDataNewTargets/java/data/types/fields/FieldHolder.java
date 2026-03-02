package data.types.fields;

@SuppressWarnings("unused")
public class FieldHolder {

    private static String staticField = "";
    private String instanceField = "";

    public static String getStaticField() {
        return staticField;
    }

    public static void setStaticField(final String value) {
        staticField = value;
    }

    public String getInstanceField() {
        return this.instanceField;
    }

    public void setInstanceField(final String value) {
        this.instanceField = value;
    }
}

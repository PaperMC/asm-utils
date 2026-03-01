package data.fields;

import data.types.fields.FieldHolder;

@SuppressWarnings({"unused", "StringOperationCanBeSimplified"})
public final class FieldToMethodSameOwnerUser {

    public static void entry() {
        final String s = FieldHolder.staticField;
        FieldHolder.staticField = new String("other");

        final FieldHolder holder = new FieldHolder();
        final String s2 = holder.instanceField;
        holder.instanceField = new String("other");
    }
}

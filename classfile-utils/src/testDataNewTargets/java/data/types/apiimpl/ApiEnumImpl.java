package data.types.apiimpl;

import io.papermc.asm.rules.classes.LegacyEnum;

public final class ApiEnumImpl implements ApiEnum, LegacyEnum<ApiEnumImpl> {

    private static int count = 0;

    private final String key;
    private final int ordinal;

    ApiEnumImpl(final String key) {
        this.key = key;
        this.ordinal = count++;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String name() {
        return this.key;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    @Override
    public int compareTo(final ApiEnumImpl o) {
        return this.ordinal - o.ordinal;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public static ApiEnum[] values() {
        final ApiEnum[] values = new ApiEnum[3];
        values[0] = ApiEnum.A;
        values[1] = ApiEnum.B;
        values[2] = ApiEnum.C;
        return values;
    }

    public static ApiEnum valueOf(final String name) {
        return switch (name) {
            case "A" -> ApiEnum.A;
            case "B" -> ApiEnum.B;
            case "C" -> ApiEnum.C;
            default -> throw new IllegalArgumentException("No value exists for name " + name);
        };
    }
}

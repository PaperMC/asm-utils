package data.classes;

import data.types.apiimpl.ApiEnum;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UseOfSystemOutOrSystemErr", "UnnecessaryToStringCall"})
public final class EnumToInterfaceUser {

    public static void entry() {
        final ApiEnum a = ApiEnum.A;
        // final String key = a.getKey();
        // System.out.println(key);
        //
        final Function<ApiEnum, String> getKey = ApiEnum::getKey;
        final String lambdaKey = getKey.apply(a);
        System.out.println(lambdaKey);

        final Supplier<String> getKeySupplier = a::getKey;
        final String keySupplier = getKeySupplier.get();
        System.out.println(keySupplier);

        final String keyStatic = ApiEnum.getKeyStatic();
        System.out.println(keyStatic);

        final Supplier<String> getKeyStaticSupplier = ApiEnum::getKeyStatic;
        final String keyStaticSupplier = getKeyStaticSupplier.get();
        System.out.println(keyStaticSupplier);

        System.out.println(a.compareTo(ApiEnum.B));
        System.out.println(ApiEnum.B.compareTo(a));

        System.out.println(ApiEnum.C.name());
        // final Function<ApiEnum, String> name = ApiEnum::name; // cannot rewrite these because references to ApiEnum are lost in the bytecode
        // System.out.println(name.apply(ApiEnum.C));
        // final Supplier<String> nameSupplier = ApiEnum.C::name;
        // System.out.println(nameSupplier.get());
        System.out.println(ApiEnum.C.ordinal());
        System.out.println(ApiEnum.A.toString());
        System.out.println(ApiEnum.A.getDeclaringClass());
        //
        System.out.println(Arrays.toString(ApiEnum.values()));
        System.out.println(ApiEnum.valueOf("A"));
    }
}

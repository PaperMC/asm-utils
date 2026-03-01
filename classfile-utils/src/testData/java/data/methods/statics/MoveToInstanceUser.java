package data.methods.statics;

import data.types.apiimpl.ApiInterface;
import data.types.apiimpl.ApiInterfaceImpl;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class MoveToInstanceUser {

    public static void entry() {
        final ApiInterface apiInterface = get();
        final String s = apiInterface.get();
        System.out.println(s);

        final Supplier<String> get = apiInterface::get;
        final String s2 = get.get();
        System.out.println(s2);

        final Function<ApiInterface, String> get2 = ApiInterface::get;
        final String s3 = get2.apply(apiInterface);
        System.out.println(s3);
    }

    private static ApiInterface get() {
        return new ApiInterfaceImpl();
    }
}

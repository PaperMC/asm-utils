package data.methods.inplace;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class SubTypeReturnUser {
    public static void entry() {
        final Methods methods = new Methods();
        final Entity player = methods.get();
        final Entity player2 = Methods.getStatic();

        final Supplier<Entity> get = methods::get;
        final Entity player3 = get.get();

        final Function<Methods, Entity> get2 = Methods::get;
        final Entity player4 = get2.apply(methods);

        final Supplier<Entity> getStatic = Methods::getStatic;
        final Entity player5 = getStatic.get();
    }
}

package data.methods.statics.param;

import data.methods.Methods;
import data.types.hierarchy.loc.Location;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unused")
final class ParamDirectUser {

    public static void entry() {
        final Location loc = new Location(1, 2, 3);
        final boolean b = Methods.consumeLocStatic(loc);

        final Methods methods = new Methods();
        final boolean b1 = methods.consumeLoc(loc);

        final Methods.Wrapper wrapper = new Methods.Wrapper(loc);

        final Function<Location, Boolean> consumeLocStatic = Methods::consumeLocStatic;
        final Boolean b2 = consumeLocStatic.apply(loc);

        final BiFunction<Methods, Location, Boolean> consumeLoc = Methods::consumeLoc;
        final Boolean b3 = consumeLoc.apply(methods, loc);

        final Function<Location, Boolean> consumeLoc2 = methods::consumeLoc;
        final Boolean b4 = consumeLoc2.apply(loc);

        final Function<Location, Methods.Wrapper> newWrapper = Methods.Wrapper::new;
        newWrapper.apply(loc);
    }
}

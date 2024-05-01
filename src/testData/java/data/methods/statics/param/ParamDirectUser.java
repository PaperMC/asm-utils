package data.methods.statics.param;

import data.methods.Methods;
import data.types.hierarchy.loc.Location;

@SuppressWarnings("unused")
final class ParamDirectUser {

    public static void entry() {
        final Location loc = new Location(1, 2, 3);
        final boolean b = Methods.consumeLocStatic(loc);

        final Methods methods = new Methods();
        final boolean b1 = methods.consumeLoc(loc);
    }
}

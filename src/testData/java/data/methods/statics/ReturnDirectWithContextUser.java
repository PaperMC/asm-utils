package data.methods.statics;

import data.methods.Methods;
import data.types.hierarchy.loc.Location;

@SuppressWarnings("unused")
final class ReturnDirectWithContextUser {

    public static void entry() {
        final Location loc = Methods.getLocStatic();
        loc.position();
        loc.location();
        System.out.println(loc);

        final Methods methods = new Methods();
        final Location loc1 = methods.getLoc();
        loc1.position();
        loc1.location();
        System.out.println(loc1);
    }
}

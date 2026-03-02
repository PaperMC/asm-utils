package data.methods.statics.returns;

import data.methods.Methods;
import data.types.hierarchy.loc.Location;
import java.util.function.Supplier;

@SuppressWarnings("unused")
final class ReturnDirectUser {

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

        final Supplier<Location> getLocStatic = Methods::getLocStatic;
        final Location loc2 = getLocStatic.get();
        loc2.position();
        loc2.location();
        System.out.println(loc2);

        final Supplier<Location> getLoc = methods::getLoc;
        final Location loc3 = getLoc.get();
        loc3.position();
        loc3.location();
        System.out.println(loc3);
    }
}

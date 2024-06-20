package data.methods.statics.param;

import data.methods.Methods;
import data.types.hierarchy.loc.Location;
import data.types.hierarchy.loc.Position;
import data.types.hierarchy.loc.PositionImpl;

@SuppressWarnings("unused")
public final class ParamFuzzyUser {
    public static void entry() {
        final Location loc = new Location(1, 2, 3);
        final boolean b = Methods.consumePosStatic(loc);

        final Position pos = new PositionImpl(1, 2, 3);
        final boolean bb = Methods.consumePosStatic(pos);

        final Methods methods = new Methods();
        final boolean b1 = methods.consumePos(loc);
        final boolean bb1 = methods.consumePos(pos);

        new Methods.PosWrapper(pos);
        new Methods.PosWrapper(loc);
    }
}

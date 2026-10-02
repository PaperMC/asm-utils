package data.methods.statics.param;

import data.methods.Methods;
import data.types.hierarchy.loc.Location;
import data.types.hierarchy.loc.Position;
import data.types.hierarchy.loc.PositionImpl;
import java.util.function.BiFunction;
import java.util.function.Function;

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

        new Methods.PosWrapper(loc);
        new Methods.PosWrapper(pos);

        final Function<Position, Boolean> consumeLocStatic = Methods::consumePosStatic;
        final Boolean b2 = consumeLocStatic.apply(loc);
        final Boolean bb2 = consumeLocStatic.apply(pos);

        final BiFunction<Methods, Position, Boolean> consumeLoc = Methods::consumePos;
        final Boolean b3 = consumeLoc.apply(methods, loc);
        final boolean bb3 = consumeLoc.apply(methods, pos);

        final Function<Position, Boolean> consumeLoc2 = methods::consumePos;
        final Boolean b4 = consumeLoc2.apply(loc);
        final Boolean bb4 = consumeLoc2.apply(pos);

        final Function<Position, Methods.PosWrapper> newWrapper = Methods.PosWrapper::new;
        newWrapper.apply(loc);
        newWrapper.apply(pos);
    }
}

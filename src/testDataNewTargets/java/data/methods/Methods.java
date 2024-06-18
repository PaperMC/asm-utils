package data.methods;

import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import data.types.hierarchy.loc.Position;
import data.types.hierarchy.loc.PositionImpl;

@SuppressWarnings("unused")
public class Methods {

    public Player get() {
        return new Player();
    }

    public void consume(final Entity entity) {
        entity.getName();
    }

    public static Player getStatic() {
        return new Player();
    }

    public static void consumeStatic(final Entity entity) {
    }

    public Position getLoc() {
        return new PositionImpl(1, 2, 3);
    }

    public static Position getLocStatic() {
        return new PositionImpl(1, 2, 3);
    }

    public boolean consumeLoc(final Position pos) {
        pos.position();
        System.out.println(pos.getClass());
        return true;
    }

    public static boolean consumeLocStatic(final Position pos) {
        pos.position();
        System.out.println(pos.getClass());
        return true;
    }

    public boolean consumePos(final Position position) {
        position.position();
        System.out.println(position.getClass());
        return true;
    }

    public static boolean consumePosStatic(final Position position) {
        position.position();
        System.out.println(position.getClass());
        return true;
    }
}

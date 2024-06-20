package data.methods;

import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import data.types.hierarchy.loc.Location;
import data.types.hierarchy.loc.Position;

@SuppressWarnings("unused")
public class Methods {

    public Entity get() {
        return new Player();
    }

    public void consume(final Player player) {
        player.getName();
    }

    public static Entity getStatic() {
        return new Player();
    }

    public static void consumeStatic(final Player player) {
    }

    public Location getLoc() {
        return new Location(1, 2, 3);
    }

    public static Location getLocStatic() {
        return new Location(1, 2, 3);
    }

    public boolean consumeLoc(final Location location) {
        location.position();
        location.location();
        return true;
    }

    public static boolean consumeLocStatic(final Location location) {
        location.position();
        location.location();
        return true;
    }

    public boolean consumePos(final Position position) {
        position.position();
        return true;
    }

    public static boolean consumePosStatic(final Position position) {
        position.position();
        return true;
    }

    public static class Wrapper {

        public Wrapper(Player player) {
        }

        public Wrapper(Location location) {
        }
    }

    public static class PosWrapper {

        public PosWrapper(Position position) {
        }
    }
}

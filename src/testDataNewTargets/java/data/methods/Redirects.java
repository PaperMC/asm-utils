package data.methods;

import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import data.types.hierarchy.loc.Location;
import data.types.hierarchy.loc.Position;
import data.types.hierarchy.loc.PositionImpl;

@SuppressWarnings("unused")
public final class Redirects {

    public static void addEntity(final Player player, final Entity entity) {
        player.addEntity(entity);
    }

    public static void addEntityStatic(final Entity entity) {
        entity.getName();
    }

    public static Position toPosition(final Location location) {
        return new PositionImpl(location.x(), location.y(), location.z());
    }

    public static Location wrapPosition(final Position input) {
        return new Location(input.x(), input.y(), input.z(), "wrapped");
    }

    public static Location wrapPositionWithContext(final Methods methods, final Position input) {
        return new Location(input.x(), input.y(), input.z(), "ctx=" + methods + " wrapped");
    }

    public static Position toPositionFuzzy(final Object maybeLocation) {
        if (maybeLocation instanceof final Position pos) {
            System.out.println("was pos");
            return pos;
        }
        System.out.println("was not pos");
        return toPosition((Location) maybeLocation);
    }

    public static void wrapObject(final Object object) {
    }
}

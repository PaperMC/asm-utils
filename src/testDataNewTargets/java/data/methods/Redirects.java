package data.methods;

import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;

@SuppressWarnings("unused")
public final class Redirects {

    public static void addEntity(final Player player, final Entity entity) {
        player.addEntity(entity);
    }

    public static void addEntityStatic(final Entity entity) {
        entity.getName();
    }
}

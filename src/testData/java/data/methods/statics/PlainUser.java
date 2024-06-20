package data.methods.statics;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;

@SuppressWarnings("unused")
final class PlainUser {

    public static void entry() {
        final Player player = new Player();
        final Entity entity = new Player();
        player.addEntity(entity);
        Player.addEntityStatic(player);

        new Methods.Wrapper(player);
    }
}

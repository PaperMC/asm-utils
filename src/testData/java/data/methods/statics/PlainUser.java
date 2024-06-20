package data.methods.statics;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
final class PlainUser {

    public static void entry() {
        final Player player = new Player();
        final Entity entity = new Player();
        player.addEntity(entity);
        Player.addEntityStatic(player);

        new Methods.Wrapper(player);

        final BiConsumer<Player, Entity> addEntity = Player::addEntity;
        addEntity.accept(player, entity);

        final Consumer<Entity> addEntity2 = player::addEntity;
        addEntity2.accept(entity);

        final Consumer<Entity> addEntityStatic = Player::addEntityStatic;
        addEntityStatic.accept(entity);

        final Function<Player, Methods.Wrapper> wrapper = Methods.Wrapper::new;
        wrapper.apply(player);
    }
}

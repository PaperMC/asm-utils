package data.methods.statics;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
final class PlainUser {

    public static void entry() {
        final Player player = new Player();
        final Entity entity = new Player();
        player.addEntity(entity);
        Player.addEntityStatic(player);

        new Methods.Wrapper(player);
        new StringBuilder(new Methods.Wrapper(new Player()).toString());

        new Methods.Wrapper(new Methods.Wrapper((Methods.Wrapper) null).getPlayer());

        final BiConsumer<Player, Entity> addEntity = Player::addEntity;
        addEntity.accept(player, entity);

        final Consumer<Entity> addEntity2 = player::addEntity;
        addEntity2.accept(entity);

        final BiConsumer<Player, Entity> addEntityAndPlayer = player::addEntityAndPlayer;
        addEntityAndPlayer.accept(player, entity);

        final Consumer<Entity> addEntityStatic = Player::addEntityStatic;
        addEntityStatic.accept(entity);

        final BiConsumer<Player, Entity> addEntityAndPlayerStatic = Player::addEntityAndPlayerStatic;
        addEntityAndPlayerStatic.accept(player, entity);

        final Function<Player, Methods.Wrapper> wrapper = Methods.Wrapper::new;
        wrapper.apply(player);
    }
}

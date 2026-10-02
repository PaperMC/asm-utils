package data.types.hierarchy;

import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
public class Player implements Entity {

    private @Nullable Entity owner = null;

    @Override
    public String getName() {
        return "Player";
    }

    public void addEntity(final Entity entity) {
        entity.getName();
    }

    public void addEntityAndPlayer(final Player player, final Entity entity) {
        entity.getName();
        player.getName();
    }

    public static void addEntityStatic(final Entity entity) {
        entity.getName();
    }

    public static void addEntityAndPlayerStatic(final Player player, final Entity entity) {
        player.getName();
        entity.getName();
    }

    void test() {
    }

    @Override
    public void setOwner(final @Nullable Entity entity) {
        this.owner = entity;
    }

    @Override
    public void setOwner(final @Nullable Player player) {
        this.owner = player;
    }

    @Override
    public @Nullable Entity getOwner() {
        return this.owner;
    }
}

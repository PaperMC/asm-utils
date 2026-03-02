package data.types.hierarchy;

import org.jspecify.annotations.Nullable;

@SuppressWarnings({"unused", "DataFlowIssue"})
public class Player implements Entity {

    public Player() {
    }

    String data = "";
    public Player(final String data) {
        this.data = data;
    }

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

    @Override
    public String toString() {
        return this.data + super.toString();
    }

    private @Nullable Entity owner;

    @Override
    public void setOwnerNew(final @Nullable Entity entity) {
        System.out.println("Set owner to " + entity + " on Player");
        this.owner = entity;
        if (this.owner != null) {
            this.owner.getName();
            assert this.owner instanceof Entity;
        }
    }

    @Override
    public void setOwner(final @Nullable Player player) {
        System.out.println("Set player owner to " + player + " on Player");
        this.owner = player;
        if (this.owner != null) {
            this.owner.getName();
            assert this.owner instanceof Player;
        }
    }

    @Override
    public @Nullable Entity getOwner() {
        return this.owner;
    }
}

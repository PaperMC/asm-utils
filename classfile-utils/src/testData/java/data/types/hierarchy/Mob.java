package data.types.hierarchy;

import org.jspecify.annotations.Nullable;

public class Mob implements Entity {

    private @Nullable Entity owner = null;

    @Override
    public String getName() {
        return "MOB";
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

package data.types.hierarchy;

import org.jspecify.annotations.Nullable;

@SuppressWarnings("ConstantValue")
public class Mob implements Entity {

    private @Nullable Entity owner = null;

    @Override
    public String getName() {
        return "MOB";
    }

    @Override
    public void setOwnerNew(final @Nullable Entity entity) {
        System.out.println("Set entity owner to " + entity + " on Mob");
        this.owner = entity;
        if (this.owner != null) {
            assert this.owner instanceof Entity;
        }
    }

    @Override
    public void setOwner(final @Nullable Player player) {
        System.out.println("Set player owner to " + player + " on Mob");
        this.owner = player;
        if (this.owner != null) {
            assert this.owner instanceof Player;
        }
    }

    @Override
    public @Nullable Entity getOwner() {
        return this.owner;
    }
}

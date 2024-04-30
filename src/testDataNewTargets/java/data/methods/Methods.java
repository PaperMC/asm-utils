package data.methods;

import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;

@SuppressWarnings("unused")
public class Methods {

    public Player get() {
        return new Player();
    }

    public void consume(final Entity entity) {
        entity.getName();
    }

    public static Player getStatic() {
        return new Player();
    }

    public static void consumeStatic(final Entity entity) {
    }
}

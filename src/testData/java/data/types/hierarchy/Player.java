package data.types.hierarchy;

public class Player implements Entity {

    @Override
    public String getName() {
        return "Player";
    }

    public void addEntity(final Entity entity) {
        entity.getName();
    }

    public static void addEntityStatic(final Entity entity) {
        entity.getName();
    }
}

package data.types.hierarchy;

@SuppressWarnings("unused")
public class Player implements Entity {

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
}

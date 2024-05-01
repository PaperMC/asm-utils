package data.types.hierarchy;

@SuppressWarnings("unused")
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

    public static void addEntityStatic(final Entity entity) {
        entity.getName();
    }

    @Override
    public String toString() {
        return this.data + super.toString();
    }
}

package data.types.hierarchy.loc;

public class Location implements Position {

    private int x;
    private int y;
    private int z;

    public Location(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int x() {
        return this.x;
    }

    @Override
    public int y() {
        return this.y;
    }

    @Override
    public int z() {
        return this.z;
    }

    @Override
    public void position() {
    }

    public void location() {
    }
}

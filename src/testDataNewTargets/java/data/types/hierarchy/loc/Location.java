package data.types.hierarchy.loc;

public class Location {

    private int x;
    private int y;
    private int z;
    private String data;

    public Location(final int x, final int y, final int z) {
    }
    public Location(final int x, final int y, final int z, final String data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public int z() {
        return this.z;
    }

    public void position() {
    }

    public void location() {
    }

    @Override
    public String toString() {
        return this.data + super.toString();
    }
}

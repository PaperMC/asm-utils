package data;

public class SameClassTarget {
    public static final InnerCls B = new InnerCls("B");

    private record InnerCls(String s) {}
}

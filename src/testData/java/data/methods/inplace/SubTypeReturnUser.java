package data.methods.inplace;

import data.methods.Methods;
import data.types.hierarchy.Entity;
import data.types.hierarchy.Player;

@SuppressWarnings("unused")
public final class SubTypeReturnUser {
    public static void entry() {
        final Methods methods = new Methods();
        final Entity player = methods.get();
        final Entity player2 = Methods.getStatic();
    }
}

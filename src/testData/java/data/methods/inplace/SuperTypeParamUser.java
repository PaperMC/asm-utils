package data.methods.inplace;

import data.methods.Methods;
import data.types.hierarchy.Player;

@SuppressWarnings("unused")
public final class SuperTypeParamUser {

    public static void run() {
        final Methods methods = new Methods();
        methods.consume(new Player());
        Methods.consumeStatic(new Player());
    }
}

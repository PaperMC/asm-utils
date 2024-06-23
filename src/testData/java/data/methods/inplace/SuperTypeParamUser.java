package data.methods.inplace;

import data.methods.Methods;
import data.types.hierarchy.Player;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class SuperTypeParamUser {

    public static void entry() {
        final Methods methods = new Methods();
        methods.consume(new Player());
        Methods.consumeStatic(new Player());

        final Consumer<Player> consume = methods::consume;
        consume.accept(new Player());

        final BiConsumer<Methods, Player> consume2 = Methods::consume;
        consume2.accept(methods, new Player());

        final Consumer<Player> consumeStatic = Methods::consumeStatic;
        consumeStatic.accept(new Player());
    }
}

package io.papermc.classfile.method.transform;

import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public final class TrackingConsumer<T> implements Consumer<T> {

    private final Consumer<T> wrapped;
    @Nullable Instance primed = null;
    boolean called = false;

    TrackingConsumer(final Consumer<T> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void accept(final T t) {
        this.wrapped.accept(t);
        this.called = true;
    }

    public Instance prime() {
        this.primed = new Instance();
        return this.primed;
    }

    private void verify() {
        if (!this.called) {
            throw new IllegalStateException("Consumer was not called");
        }
    }

    public final class Instance implements AutoCloseable {

        @Override
        public void close() {
            TrackingConsumer.this.verify();
        }
    }
}

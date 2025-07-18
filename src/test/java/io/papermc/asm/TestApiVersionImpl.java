package io.papermc.asm;

public record TestApiVersionImpl(int version) implements io.papermc.asm.versioned.ApiVersion<TestApiVersionImpl> {

    @Override
    public int compareTo(final TestApiVersionImpl o) {
        return Integer.compare(this.version, o.version);
    }
}

package io.papermc.asm;

import java.util.List;

public final class ApiVersions {

    public static final TestApiVersionImpl ONE = new TestApiVersionImpl(1);
    public static final TestApiVersionImpl TWO = new TestApiVersionImpl(2);
    public static final TestApiVersionImpl THREE = new TestApiVersionImpl(3);
    public static final TestApiVersionImpl FOUR = new TestApiVersionImpl(4);
    public static final List<TestApiVersionImpl> ALL_VERSIONS = List.of(ONE, TWO, THREE, FOUR);

    private ApiVersions() {
    }
}

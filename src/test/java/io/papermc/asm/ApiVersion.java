package io.papermc.asm;

import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ApiVersion implements io.papermc.asm.versioned.ApiVersion {

    public static final ApiVersion ONE = new ApiVersion(1);
    public static final ApiVersion TWO = new ApiVersion(2);
    public static final ApiVersion THREE = new ApiVersion(3);
    public static final ApiVersion FOUR = new ApiVersion(4);

    public static final List<ApiVersion> ALL_VERSIONS = List.of(ONE, TWO, THREE, FOUR);

    private final int version;

    public ApiVersion(final int version) {
        this.version = version;
    }

    @Override
    public int compareTo(final io.papermc.asm.versioned.ApiVersion o) {
        return Integer.compare(this.version, ((ApiVersion) o).version);
    }

    @Override
    public String toString() {
        return "ApiVersion{" +
            "version=" + this.version +
            '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final ApiVersion that = (ApiVersion) o;
        return this.version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.version);
    }
}

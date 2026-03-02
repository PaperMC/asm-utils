package data.types.hierarchy;

import org.jspecify.annotations.Nullable;

public interface Entity {

    String getName();

    void setOwnerNew(@Nullable Entity entity);

    void setOwner(@Nullable Player player);

    @Nullable Entity getOwner();
}

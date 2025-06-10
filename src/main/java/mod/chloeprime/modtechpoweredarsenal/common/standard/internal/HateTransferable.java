package mod.chloeprime.modtechpoweredarsenal.common.standard.internal;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface HateTransferable {
    Entity mtpa$getHateOwner();
    void mtpa$setHateOwner(Entity entity);
}

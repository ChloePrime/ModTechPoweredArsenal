package mod.chloeprime.modtechpoweredarsenal.common.standard.internal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TraceableEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface HateTransferable extends TraceableEntity {
    Entity mtpa$getHateOwner();
    void mtpa$setHateOwner(Entity entity);
}

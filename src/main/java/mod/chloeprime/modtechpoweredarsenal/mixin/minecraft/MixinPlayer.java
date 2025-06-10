package mod.chloeprime.modtechpoweredarsenal.mixin.minecraft;

import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.HateTransferable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Optional;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity implements HateTransferable {
    private @Unique WeakReference<Entity> mtpa$owner;

    @Override
    public Entity mtpa$getHateOwner() {
        return Optional.ofNullable(mtpa$owner).map(Reference::get).orElse(this);
    }

    @Override
    public void mtpa$setHateOwner(Entity entity) {
        mtpa$owner = new WeakReference<>(entity);
    }

    protected MixinPlayer(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
}

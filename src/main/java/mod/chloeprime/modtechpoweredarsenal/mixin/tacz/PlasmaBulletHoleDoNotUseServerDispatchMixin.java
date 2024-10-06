package mod.chloeprime.modtechpoweredarsenal.mixin.tacz;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.modtechpoweredarsenal.common.standard.attachments.PlasmaVisualDispatcher;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EntityKineticBullet.class, remap = false)
public abstract class PlasmaBulletHoleDoNotUseServerDispatchMixin extends Projectile {

    @WrapOperation(
            method = "onHitBlock",
            at = @At(
                    value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"
            ))
    private <T extends ParticleOptions> int plasmaBulletHoleDoNotUseServerDispatch(ServerLevel instance, T particle, double v, double pType, double pPosX, int pPosY, double pPosZ, double pParticleCount, double pXOffset, double pYOffset, Operation<Integer> original) {
        return PlasmaVisualDispatcher.onServerSpawnBulletHole(this, particle, () -> original.call(instance, particle, v, pType, pPosX, pPosY, pPosZ, pParticleCount, pXOffset, pYOffset));
    }

    public PlasmaBulletHoleDoNotUseServerDispatchMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
}

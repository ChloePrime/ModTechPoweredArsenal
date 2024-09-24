package mod.chloeprime.modtechpoweredarsenal.common.standard.ammo;

import com.google.common.collect.Sets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.FangEmitter;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.Shockwave;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

@Mod.EventBusSubscriber
public class ShockwaveGrenadeBehavior {
    public static final Set<ResourceLocation> EVK_FANG_GUNS = Sets.newConcurrentHashSet(Set.of(
            ModTechPoweredArsenal.loc("gl_shark")
    ));
    public static final Set<ResourceLocation> SHOCKWAVE_GUNS = Sets.newConcurrentHashSet(Set.of(
            ModTechPoweredArsenal.loc("gl_deafening_whisper")
    ));

    @SubscribeEvent
    public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
        var bullet = event.getAmmo();
        if (bullet.level().isClientSide) {
            return;
        }
        if (event.getHitResult().getDirection() != Direction.UP) {
            return;
        }
        var velocity = bullet.getDeltaMovement().with(Direction.Axis.Y, 0);
        if (velocity.lengthSqr() <= 1e-6) {
            return;
        }

        var damage = TimelessAPI.getCommonGunIndex(bullet.getGunId()).map(
                index -> {
                    var directHit = index.getBulletData().getDamageAmount();
                    var explosion = 0.5F * Optional.ofNullable(index.getBulletData().getExplosionData())
                            .map(ExplosionData::getDamage)
                            .orElse(0F);
                    return OptionalDouble.of(directHit + explosion);
                }
        ).orElse(OptionalDouble.empty());

        if (EVK_FANG_GUNS.contains(bullet.getGunId())) {
            var emitterSpeed = 0.5;
            var angleStep = Math.toRadians(36);
            var baseDirection = velocity.normalize();
            for (int i = -1; i <= 1; i++) {
                var direction = baseDirection.yRot((float) (i * angleStep));
                var emitter = new FangEmitter(bullet.level(), bullet.getOwner());
                emitter.setYRot(bullet.getYRot());
                emitter.setPos(event.getHitResult().getLocation());
                emitter.setDeltaMovement(direction.scale(emitterSpeed));
                damage.ifPresent(emitter::setDamage);
                bullet.level().addFreshEntity(emitter);
            }
        } else if (SHOCKWAVE_GUNS.contains(bullet.getGunId())) {
            var emitterSpeed = 0.7;
            var emitter = new Shockwave(bullet.level(), bullet.getOwner());
            var direction = velocity.normalize();
            emitter.setYRot(bullet.getYRot());
            emitter.setPos(event.getHitResult().getLocation());
            emitter.setDeltaMovement(direction.scale(emitterSpeed));
            damage.ifPresent(emitter::setDamage);
            bullet.level().addFreshEntity(emitter);
        }
    }
}

package mod.chloeprime.modtechpoweredarsenal.common.standard.ammo;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.FangEmitter;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.Shockwave;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.AttachmentHolder;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.GunPreconditions;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class ShockwaveGrenadeBehavior {
    public static final Map<AttachmentHolder, Object> CHAIN_REACTION_ATTACHMENTS = new ConcurrentHashMap<>(Map.of(
            new AttachmentHolder(ModTechPoweredArsenal.loc("ammo_trait_chain_action"), AttachmentType.EXTENDED_MAG), Boolean.TRUE
    ));
    public static final Set<ResourceLocation> EVK_FANG_GUNS = Sets.newConcurrentHashSet(Set.of(
            ModTechPoweredArsenal.loc("gl_shark")
    ));
    public static final Set<ResourceLocation> SHOCKWAVE_GUNS = Sets.newConcurrentHashSet(Set.of(
            ModTechPoweredArsenal.loc("gl_deafening_whisper")
    ));

    public static final String PDK_CHAIN_DAMAGE = ModTechPoweredArsenal.loc("chain_reaction_damage").toString();
    public static final String PDK_CHAIN_RANGE = ModTechPoweredArsenal.loc("chain_reaction_range").toString();

    @SubscribeEvent
    public static void onBulletCreate(BulletCreateEvent event) {
        if (event.getBullet().level().isClientSide) {
            return;
        }
        var gun = event.getGun();
        var kun = IGun.getIGunOrNull(gun);
        if (kun == null) {
            return;
        }
        var gid = kun.getGunId(gun);
        var isValidWeapon = EVK_FANG_GUNS.contains(gid) || SHOCKWAVE_GUNS.contains(gid);
        if (!isValidWeapon) {
            return;
        }
        // 将爆炸数据作为连锁爆炸数据记录到子弹中
        TimelessAPI.getCommonGunIndex(gid).ifPresent(
                index -> CHAIN_REACTION_ATTACHMENTS.keySet().stream()
                        .filter(ath -> GunPreconditions.hasAttachmentInstalled(gun, ath.type(), ath.id()))
                        .findFirst()
                        .ifPresent(ignoredAth -> {
                            var explosionData = Optional.ofNullable(index.getBulletData().getExplosionData());
                            var damage = explosionData
                                    .map(ExplosionData::getDamage)
                                    .orElse(index.getBulletData().getDamageAmount());
                            var range = explosionData
                                    .map(ExplosionData::getRadius)
                                    .orElse(3F);
                            var pd = event.getBullet().getPersistentData();
                            pd.putFloat(PDK_CHAIN_DAMAGE, damage);
                            pd.putFloat(PDK_CHAIN_RANGE, range);
                        }));
    }

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
            var context = bullet.getPersistentData();
            if (context.contains(PDK_CHAIN_DAMAGE, Tag.TAG_ANY_NUMERIC) && context.contains(PDK_CHAIN_RANGE, Tag.TAG_ANY_NUMERIC)) {
                var chainDamage = context.getFloat(PDK_CHAIN_DAMAGE);
                var chainRange = context.getFloat(PDK_CHAIN_RANGE);
                emitter.enableChainReaction(chainDamage, chainRange);
            }
            bullet.level().addFreshEntity(emitter);
        }
    }
}

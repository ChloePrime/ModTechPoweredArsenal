package mod.chloeprime.modtechpoweredarsenal.common.standard.ammo;

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
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.GunHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class ShockwaveGrenadeBehavior {
    public static final Map<AttachmentHolder, Object> GREED_OF_USSR_ATTACHMENTS = new ConcurrentHashMap<>(Map.of(
            new AttachmentHolder(ModTechPoweredArsenal.loc("ammo_trait_greed_of_ussr"), AttachmentType.EXTENDED_MAG), Boolean.TRUE
    ));
    public static final Map<AttachmentHolder, Object> CHAIN_REACTION_ATTACHMENTS = new ConcurrentHashMap<>(Map.of(
            new AttachmentHolder(ModTechPoweredArsenal.loc("ammo_trait_chain_action"), AttachmentType.EXTENDED_MAG), Boolean.TRUE
    ));
    public static final Set<ResourceLocation> EVK_FANG_GUNS = Sets.newConcurrentHashSet(Set.of(
            ModTechPoweredArsenal.loc("gl_shark")
    ));
    public static final Set<ResourceLocation> SHOCKWAVE_GUNS = Sets.newConcurrentHashSet(Set.of(
            ModTechPoweredArsenal.loc("gl_deafening_whisper")
    ));

    public static final String PDK_FOOD_VALUE = ModTechPoweredArsenal.loc("greed_of_ussr.food_value").toString();
    public static final String PDK_SATURATION_VALUE = ModTechPoweredArsenal.loc("greed_of_ussr.saturation_value").toString();
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
        TimelessAPI.getCommonGunIndex(gid).ifPresent(index -> {
            CHAIN_REACTION_ATTACHMENTS.keySet().stream()
                    .filter(ath -> GunHelper.hasAttachmentInstalled(gun, ath.type(), ath.id()))
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
                    });
            GREED_OF_USSR_ATTACHMENTS.keySet().stream()
                    .filter(ath -> GunHelper.hasAttachmentInstalled(gun, ath.type(), ath.id()))
                    .findFirst()
                    .ifPresent(ignoredAth -> {
                        var pd = event.getBullet().getPersistentData();
                        pd.putInt(PDK_FOOD_VALUE, 6);
                        pd.putFloat(PDK_SATURATION_VALUE, 2.5F);
                    });
        });
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
                var context = bullet.getPersistentData();
                if (context.contains(PDK_FOOD_VALUE, Tag.TAG_ANY_NUMERIC) && context.contains(PDK_SATURATION_VALUE, Tag.TAG_ANY_NUMERIC)) {
                    var data = new CompoundTag();
                    data.put(PDK_FOOD_VALUE, Objects.requireNonNull(context.get(PDK_FOOD_VALUE)));
                    data.put(PDK_SATURATION_VALUE, Objects.requireNonNull(context.get(PDK_SATURATION_VALUE)));
                    emitter.setFangData(data);
                }
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

    public static void onFangHit(TraceableEntity fang, LivingEntity victim, boolean hurt) {
        if (!hurt || victim.level().isClientSide) {
            return;
        }
        if (!(fang instanceof Entity fangEntity)) {
            return;
        }
        if (!(fang.getOwner() instanceof Player shooter)) {
            return;
        }
        var context = fangEntity.getPersistentData();
        if (context.contains(PDK_FOOD_VALUE, Tag.TAG_ANY_NUMERIC) && context.contains(PDK_SATURATION_VALUE, Tag.TAG_ANY_NUMERIC)) {
            var food = context.getInt(PDK_FOOD_VALUE);
            var saturation = context.getFloat(PDK_SATURATION_VALUE);
            shooter.level().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.PLAYER_BURP, shooter.getSoundSource(), 0.5F, shooter.level().random.nextFloat() * 0.1F + 0.9F);
            shooter.getFoodData().eat(food, saturation);
        }
    }
}

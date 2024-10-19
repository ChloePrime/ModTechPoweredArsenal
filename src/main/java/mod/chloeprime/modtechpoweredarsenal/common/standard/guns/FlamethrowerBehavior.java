package mod.chloeprime.modtechpoweredarsenal.common.standard.guns;

import com.google.common.collect.Sets;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunDamageSourcePart;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModLoadStatus;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.lightland.MtpaL2Module;
import mod.chloeprime.modtechpoweredarsenal.common.lightland.guns.SoulFlamethrowerBehaviorLCProxy;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.DamageSourceUtil;
import mod.chloeprime.modtechpoweredarsenal.mixin.minecraft.DamageSourcesAccessor;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.Set;

@Mod.EventBusSubscriber
public class FlamethrowerBehavior {
    public static final Set<ResourceLocation> FLAMETHROWERS = Sets.newConcurrentHashSet(Set.of(
            ModTechPoweredArsenal.loc("flamethrower")
    ));
    public static final Set<ResourceLocation> SOUL_FUEL_MAGS = Sets.newConcurrentHashSet(Set.of(
            MtpaL2Module.loc("ammo_mod_soul_fuel")
    ));
    public static final String PDK_BULLET_FLAME_TYPE = ModTechPoweredArsenal.loc("flame_type").toString();
    public static final int BULLET_FLAME_TYPE_HEAT = 1;
    public static final int BULLET_FLAME_TYPE_SOUL = 2;

    public static int getType(GunInfo gun) {
        if (!FLAMETHROWERS.contains(gun.gunId())) {
            return 0;
        }
        return willShootSoulFlame(gun) ? BULLET_FLAME_TYPE_SOUL : BULLET_FLAME_TYPE_HEAT;
    }

    public static boolean willShootSoulFlame(GunInfo gun) {
        return SOUL_FUEL_MAGS.contains(gun.gunItem().getAttachmentId(gun.gunStack(), AttachmentType.EXTENDED_MAG));
    }

    @SubscribeEvent
    public static void onBulletCreate(BulletCreateEvent event) {
        if (event.getBullet().level().isClientSide) {
            return;
        }
        Optional<GunInfo> gunInfo = Gunsmith.getGunInfo(event.getGun());
        var isValidGun = gunInfo
                .map(GunInfo::gunId)
                .filter(FLAMETHROWERS::contains)
                .isPresent();
        if (!isValidGun) {
            return;
        }
        var type = getType(gunInfo.get());
        if (type <= 0) {
            return;
        }
        event.getBullet().getPersistentData().putInt(PDK_BULLET_FLAME_TYPE, type);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPreHurt(EntityHurtByGunEvent.Pre event) {
        var source1 = event.getDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING);
        var source2 = event.getDamageSource(GunDamageSourcePart.ARMOR_PIERCING);

        var bullet = event.getBullet();
        var victim = event.getHurtEntity();
        if (victim == null || bullet == null) {
            return;
        }
        var type = bullet.getPersistentData().getInt(PDK_BULLET_FLAME_TYPE);
        DamageSource newSource;
        switch (type) {
            case BULLET_FLAME_TYPE_HEAT -> {
                newSource = ((DamageSourcesAccessor) victim.damageSources()).invokeSource(DamageTypes.IN_FIRE, bullet, event.getAttacker());
            }
            case BULLET_FLAME_TYPE_SOUL -> {
                if (source1.is(DamageSourceUtil.ANY_MAGIC)) {
                    return;
                }
                newSource = victim.damageSources().indirectMagic(bullet, event.getAttacker());
            }
            default -> {
                return;
            }
        }
        event.setDamageSource(GunDamageSourcePart.NON_ARMOR_PIERCING, newSource);
        event.setDamageSource(GunDamageSourcePart.ARMOR_PIERCING, newSource);
    }

    @SubscribeEvent
    public static void onPostHurt(EntityHurtByGunEvent.Post event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        if (!(event.getHurtEntity() instanceof LivingEntity victim)) {
            return;
        }
        int type = event.getBullet().getPersistentData().getInt(PDK_BULLET_FLAME_TYPE);
        switch (type) {
            case BULLET_FLAME_TYPE_HEAT -> {
                victim.setRemainingFireTicks((int) (event.getBaseAmount() * 20));
            }
            case BULLET_FLAME_TYPE_SOUL -> {
                if (ModLoadStatus.L2C_INSTALLED) {
                    SoulFlamethrowerBehaviorLCProxy.addSoulFlameDebuff(victim, event.getAttacker());
                }
            }
        }
    }

    public static boolean disableServerBulletHole(Projectile bullet, ParticleOptions particle) {
        return bullet.getPersistentData().getInt(PDK_BULLET_FLAME_TYPE) > 0;
    }
}

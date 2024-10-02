package mod.chloeprime.modtechpoweredarsenal.common.standard.guns;

import com.tacz.guns.api.event.common.GunShootEvent;
import mod.chloeprime.gunsmithlib.api.common.GunReloadFeedEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.OptionalInt;

@Mod.EventBusSubscriber
public class OverheatMechanic {
    public static final String TAG_KEY_NEXT_LOAD_ETA = "%s:overheat.last_shoot".formatted(ModTechPoweredArsenal.MODID);
    public static final String TAG_KEY_HEAT = "%s:overheat.heat".formatted(ModTechPoweredArsenal.MODID);

    @SubscribeEvent
    public static void clearHeatOnReload(GunReloadFeedEvent.Post event) {
        if (getHeat(event.getGunInfo().gunStack()) > 0) {
            setHeat(event.getGunInfo().gunStack(), 0);
        }
    }

    /**
     * 暂时只有能量武器支持换弹散热（
     */
    public static boolean needsReloadAfterOverheat(ItemStack stack) {
        return EnergyWeaponData.runtime(stack)
                .map(EnergyWeaponData.Runtime::energy)
                .filter(EnergyWeaponData::needsReloadOnFullHeat)
                .isPresent();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cannotShotWhenHeatFull(GunShootEvent event) {
        if (isOverheat(event.getGunItemStack())) {
            event.setCanceled(true);
        }
    }

    public static boolean isOverheat(ItemStack stack) {
        var runtime = OverheatData.runtime(stack).orElse(null);
        if (runtime == null) {
            return false;
        }
        var heat = getHeat(runtime.gun().gunStack());
        var maxHeat = runtime.overheat().shotsBeforeOverheat();
        return heat >= maxHeat;
    }

    public static OptionalInt getMaxHeat(ItemStack stack) {
        return OverheatData.runtime(stack)
                .map(OverheatData.Runtime::overheat)
                .map(OverheatData::shotsBeforeOverheat)
                .map(OptionalInt::of)
                .orElse(OptionalInt.empty());
    }

    public static int getHeat(ItemStack gun) {
        return gun.hasTag()
                ? gun.getOrCreateTag().getInt(TAG_KEY_HEAT)
                : 0;
    }

    public static void setHeat(ItemStack gun, int value) {
        if (value == 0) {
            if (gun.hasTag()) {
                gun.getOrCreateTag().remove(TAG_KEY_HEAT);
            }
        } else {
            gun.getOrCreateTag().putInt(TAG_KEY_HEAT, value);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void recordLastShoot(GunShootEvent event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        var runtime = OverheatData.runtime(event.getGunItemStack()).orElse(null);
        if (runtime == null) {
            return;
        }
            var now = event.getShooter().level().getGameTime();
            var delay = runtime.overheat().partialHeatDelay();
            var tag = runtime.gun().gunStack().getOrCreateTag();
            var oldHeat = tag.getInt(TAG_KEY_HEAT);
            var maxHeat = runtime.overheat().shotsBeforeOverheat();
            var newHeat = Math.min(oldHeat + 1, maxHeat);
            tag.putLong(TAG_KEY_NEXT_LOAD_ETA, now + delay);
            tag.putInt(TAG_KEY_HEAT, newHeat);
    }

    @SubscribeEvent
    public static void cooldown(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }
        var isClient = event.player.level().isClientSide;
        var rgi = OverheatData.runtime(event.player.getMainHandItem()).orElse(null);
        if (rgi == null) {
            return;
        }
        var gun = rgi.gun();
        var overheatData = rgi.overheat();
        var tag = gun.gunStack().getOrCreateTag();
        var oldHeat = tag.getInt(TAG_KEY_HEAT);
        var maxHeat = overheatData.shotsBeforeOverheat();
        var isOverheat = oldHeat >= maxHeat;

        var etaPenalty = isOverheat
                ? overheatData.fullHeatDelay() - overheatData.partialHeatDelay()
                : 0;

        var now = event.player.level().getGameTime();
        var eta = gun.gunStack().hasTag()
                ? gun.gunStack().getOrCreateTag().getLong(TAG_KEY_NEXT_LOAD_ETA)
                : 0;

        if (now < eta + etaPenalty) {
            if (isOverheat && isClient) {
                if (now == eta - overheatData.partialHeatDelay() + 1) {
                    playCooldownSound(event.player);
                }
                var updateInterval = 2;
                var salt = event.player.hashCode();
                if ((now + salt) % updateInterval == 0) {
                    spawnCooldownSmoke(event.player);
                }
            }
            return;
        }

        if (!isClient) {
            if (isOverheat && needsReloadAfterOverheat(gun.gunStack())) {
                return;
            }
            var newHeat = Math.max(0, oldHeat - overheatData.coolCount());
            setHeat(gun.gunStack(), newHeat);
            tag.putLong(TAG_KEY_NEXT_LOAD_ETA, now + overheatData.coolDelay());
        }
    }

    private static void playCooldownSound(Entity shooter) {
        shooter.playSound(SoundEvents.FIRE_EXTINGUISH, 1, 0.8F);
    }

    private static void spawnCooldownSmoke(LivingEntity shooter) {
        var muzzle = Gunsmith.getProximityMuzzlePos(shooter);
        shooter.level().addParticle(
                ParticleTypes.POOF,
                muzzle.x(), muzzle.y(), muzzle.z(),
                0, 0.25, 0
        );
    }

}

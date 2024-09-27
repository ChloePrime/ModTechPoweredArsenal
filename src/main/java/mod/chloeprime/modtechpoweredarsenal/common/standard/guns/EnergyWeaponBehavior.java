package mod.chloeprime.modtechpoweredarsenal.common.standard.guns;

import com.tacz.guns.api.event.common.GunReloadEvent;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.GunHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class EnergyWeaponBehavior {
    public static final Map<ResourceLocation, EnergyWeaponData> DATA_MAP = new ConcurrentHashMap<>(Map.of(
            ModTechPoweredArsenal.loc("ew_scythe"), new EnergyWeaponData(200, 200 * 30 * 3, 600, 20, 60, 1)
    ));

    public static final String TAG_KEY_NEXT_LOAD_ETA = ModTechPoweredArsenal.loc("energy_weapons.last_shoot").toString();

    /**
     * 注意：eta 记录的永远是"战术装填"（非空弹匣情况下）的开始装填时间。
     * 空弹匣额外的装填时间以记录中两者装填时间的插值作为惩罚实现。
     */
    @SubscribeEvent
    public static void recordLastShoot(GunShootEvent event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        var rgi = EnergyWeaponData.runtime(event.getGunItemStack()).orElse(null);
        if (rgi == null) {
            return;
        }
        var gun = rgi.gun();
        var energyData = rgi.energy();

        var now = event.getShooter().level().getGameTime();
        var delay = energyData.tacticalCooldown();
        gun.gunStack().getOrCreateTag().putLong(TAG_KEY_NEXT_LOAD_ETA, now + delay);
    }

    @SubscribeEvent
    public static void energyWeaponCannotReload(GunReloadEvent event) {
        if (EnergyWeaponData.runtime(event.getGunItemStack()).isPresent()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void syncAndLoadAmmo(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        var rgi = EnergyWeaponData.runtime(event.player.getMainHandItem()).orElse(null);
        if (rgi == null) {
            return;
        }
        var gun = rgi.gun();
        var energyData = rgi.energy();

        var cap = gun.gunStack().getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
        if (cap == null) {
            return;
        }

        var isClient = event.player.level().isClientSide;

        var isEmpty = gun.gunItem().getCurrentAmmoCount(gun.gunStack()) == 0
                && !gun.gunItem().hasBulletInBarrel(gun.gunStack());
        var etaPenalty = isEmpty
                ? energyData.emptyMagCooldown() - energyData.tacticalCooldown()
                : 0;
        var now = event.player.level().getGameTime();
        var eta = gun.gunStack().hasTag()
                ? gun.gunStack().getOrCreateTag().getLong(TAG_KEY_NEXT_LOAD_ETA)
                : 0;
        if (now < eta + etaPenalty) {
            if (isEmpty && isClient) {
                if (now == eta - energyData.tacticalCooldown() + 1) {
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
            GunHelper.magicReload(event.player, gun.gunStack(), 1);
            gun.gunStack().getOrCreateTag().putLong(TAG_KEY_NEXT_LOAD_ETA, now + energyData.refillDelay());
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

    @Mod.EventBusSubscriber
    public static class CapAttacher {
        public static final ResourceLocation CAP_ID = ModTechPoweredArsenal.loc("energy_weapon_cap");

        @SubscribeEvent
        public static void onAttachCaps(AttachCapabilitiesEvent<ItemStack> event) {
            Gunsmith
                    .getGunInfo(event.getObject())
                    .filter(gunInfo -> DATA_MAP.containsKey(gunInfo.gunId()))
                    .ifPresent(gunInfo -> event.addCapability(CAP_ID, new CapProvider(gunInfo)));
        }
    }

    public static class CapProvider implements ICapabilityProvider, IEnergyStorage {
        private final ItemStack stack;
        private final ResourceLocation gunId;

        public CapProvider(GunInfo gun) {
            this.stack = gun.gunStack();
            this.gunId = gun.gunId();
        }

        public static final Capability<IEnergyStorage> ENERGY_CAP = ForgeCapabilities.ENERGY;
        public static final String TAG_ENERGY = ModTechPoweredArsenal.loc("energy_stored").toString();
        private final LazyOptional<IEnergyStorage> CAP_INSTANCE = LazyOptional.of(() -> this);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return ENERGY_CAP.orEmpty(cap, CAP_INSTANCE);
        }

        public void setEnergyStored(int value) {
            var data = EnergyWeaponData.runtime(stack).orElse(null);
            if (data == null) {
                stack.getOrCreateTag().putInt(TAG_ENERGY, value);
                return;
            }
            var frontend = getEnergyInFrontend();
            var backend = value - frontend;
            if (backend <= 0) {
                stack.getOrCreateTag().putInt(TAG_ENERGY, backend);
                return;
            }
            var shootCost = data.energy().shootCost();
            var dummyAmmo = backend / shootCost;
            data.gun().gunItem().setDummyAmmoAmount(data.gun().gunStack(), dummyAmmo);
            stack.getOrCreateTag().putInt(TAG_ENERGY, backend - dummyAmmo * shootCost);
        }

        public int getMaxReceive() {
            return canReceive() ? getChargePower() : 0;
        }

        public int getMaxExtract() {
            return canExtract() ? getChargePower() : 0;
        }

        private int getChargePower() {
            var data = DATA_MAP.get(gunId);
            return data != null ? data.chargePower() : 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive())
                return 0;

            int energyReceived = Math.min(getMaxEnergyStored() - getEnergyStored(), Math.min(getMaxReceive(), maxReceive));
            if (!simulate)
                setEnergyStored(getEnergyStored() + energyReceived);
            return energyReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract())
                return 0;

            int energyExtracted = Math.min(getEnergyStored(), Math.min(getMaxExtract(), maxExtract));
            if (!simulate)
                setEnergyStored(getEnergyStored() - energyExtracted);
            return energyExtracted;
        }

        @Override
        public int getEnergyStored() {
            var frontend = getEnergyInFrontend();
            var backend = getEnergyInBackend();
            var rem = stack.hasTag() ? stack.getOrCreateTag().getInt(TAG_ENERGY) : 0;
            return frontend + backend + rem;
        }

        private int getEnergyInBackend() {
            return Optional.ofNullable(DATA_MAP.get(gunId))
                    .map(EnergyWeaponData::shootCost)
                    .map(shootCost -> shootCost * Gunsmith
                            .getGunInfo(stack)
                            .map(gunInfo -> gunInfo.gunItem().getDummyAmmoAmount(gunInfo.gunStack()))
                            .orElse(0)
                    ).orElse(0);
        }

        private int getEnergyInFrontend() {
            return Optional.ofNullable(DATA_MAP.get(gunId))
                    .map(EnergyWeaponData::shootCost)
                    .map(shootCost -> shootCost * Gunsmith
                            .getGunInfo(stack)
                            .map(gunInfo -> {
                                var mag = gunInfo.gunItem().getCurrentAmmoCount(gunInfo.gunStack());
                                var barrel = gunInfo.gunItem().hasBulletInBarrel(gunInfo.gunStack()) ? 1 : 0;
                                return mag + barrel;
                            }).orElse(0)
                    ).orElse(0);
        }

        @Override
        public int getMaxEnergyStored() {
            var data = DATA_MAP.get(gunId);
            return data != null ? data.batterySize() : 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}

package mod.chloeprime.modtechpoweredarsenal.common.standard.guns;

import com.tacz.guns.api.event.common.GunReloadEvent;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.util.AttachmentDataUtils;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.GunHelper;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class EnergyWeaponBehavior {
    public static final Map<ResourceLocation, EnergyWeaponData> DATA_MAP = new ConcurrentHashMap<>(Map.of(
            ModTechPoweredArsenal.loc("ew_scythe"), new EnergyWeaponData(200, 600, true)
    ));

    public static boolean isEnergyWeapon(ItemStack stack) {
        return Gunsmith.getGunInfo(stack)
                .map(GunInfo::gunId)
                .filter(DATA_MAP::containsKey)
                .isPresent();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void energyWeaponCannotReload(GunReloadEvent event) {
        var disableReload = EnergyWeaponData.runtime(event.getGunItemStack())
                .filter(data -> !canEnergyWeaponReload(data))
                .isPresent();
        if (disableReload) {
            event.setCanceled(true);
        }
    }

    public static boolean canEnergyWeaponReload(EnergyWeaponData.Runtime data) {
        return data.energy().needsReloadOnFullHeat() && OverheatMechanic.isOverheat(data.gun().gunStack());
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

        var cap = gun.gunStack().getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
        if (cap == null) {
            return;
        }

        var isClient = event.player.level().isClientSide;
        if (!isClient && !gun.gunItem().useDummyAmmo(gun.gunStack())) {
            gun.setDummyAmmoAmount(0);
        }

        if (!isClient) {
            var needsReload = canEnergyWeaponReload(rgi);
            if (needsReload) {
                // 需要换散热器时，将弹匣转移至备弹
                var ammo = gun.getTotalAmmo();
                if (ammo > 0) {
                    gun.gunItem().setCurrentAmmoCount(gun.gunStack(), 0);
                    gun.gunItem().setBulletInBarrel(gun.gunStack(), false);
                    gun.addDummyAmmoAmount(ammo);
                }
            } else {
                // 能开火时，将备弹转移至弹匣
                if (gun.getTotalAmmo() != gun.getTotalMagazineSize()) {
                    var ammo = gun.getDummyAmmoAmount();
                    GunHelper.magicReload(event.player, gun.gunStack(), ammo);
                }
            }
        }
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
            var totalAmmoAmount = value / data.energy().energyPerShot();
            var rem = value - totalAmmoAmount * data.energy().energyPerShot();

            var needsReload = canEnergyWeaponReload(data);
            if (needsReload || totalAmmoAmount == 0) {
                data.gun().gunItem().setCurrentAmmoCount(data.gun().gunStack(), 0);
                data.gun().gunItem().setBulletInBarrel(data.gun().gunStack(), false);
                data.gun().setDummyAmmoAmount(totalAmmoAmount);
            } else {
                var hasBarrel = data.gun().index().getGunData().getBolt() != Bolt.OPEN_BOLT;
                if (hasBarrel) {
                    data.gun().gunItem().setBulletInBarrel(data.gun().gunStack(), true);
                }
                var totalBulletExcludeBarrel = totalAmmoAmount - (hasBarrel ? 1 : 0);
                var gunpackMagSize = AttachmentDataUtils.getAmmoCountWithAttachment(data.gun().gunStack(), data.gun().index().getGunData());
                var magAmmoAmount = Math.min(totalBulletExcludeBarrel, gunpackMagSize);
                var batAmmoAmount = Math.max(totalBulletExcludeBarrel - magAmmoAmount, 0);

                data.gun().gunItem().setCurrentAmmoCount(data.gun().gunStack(), magAmmoAmount);
                data.gun().setDummyAmmoAmount(batAmmoAmount);
            }

            stack.getOrCreateTag().putInt(TAG_ENERGY, rem);
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
            return EnergyWeaponData.runtime(stack)
                    .map(data -> data.energy().energyPerShot() * data.gun().gunItem().getDummyAmmoAmount(data.gun().gunStack()))
                    .orElse(0);
        }

        private int getEnergyInFrontend() {
            return EnergyWeaponData.runtime(stack)
                    .map(data -> data.energy().energyPerShot() * GunHelper.getTotalAmmo(data.gun()))
                    .orElse(0);
        }

        @Override
        public int getMaxEnergyStored() {
            return EnergyWeaponData.runtime(stack)
                    .map(data -> data.energy().energyPerShot() * GunHelper.getTotalMagSize(data.gun()))
                    .orElse(0);
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

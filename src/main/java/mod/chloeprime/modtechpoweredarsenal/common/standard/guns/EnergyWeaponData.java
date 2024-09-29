package mod.chloeprime.modtechpoweredarsenal.common.standard.guns;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record EnergyWeaponData(
        int shootCost,
        int batterySize,
        int chargePower,
        int tacticalCooldown,
        int emptyMagCooldown,
        int refillDelay,
        boolean needsReloadOnFullHeat
) {
    public record RuntimeEnergyWeaponData(
            EnergyWeaponData energy,
            GunInfo gun
    ) {
    }

    public static Optional<RuntimeEnergyWeaponData> runtime(ItemStack stack) {
        return Gunsmith
                .getGunInfo(stack)
                .flatMap(gi -> Optional
                        .ofNullable(EnergyWeaponBehavior.DATA_MAP.get(gi.gunId()))
                        .map(ei -> new RuntimeEnergyWeaponData(ei, gi))
                );
    }
}

package mod.chloeprime.modtechpoweredarsenal.common.standard.guns;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record EnergyWeaponData(
        int energyPerShot,
        int chargePower,
        boolean needsReloadOnFullHeat
) {
    public record Runtime(
            EnergyWeaponData energy,
            GunInfo gun
    ) {
    }

    public static Optional<Runtime> runtime(ItemStack stack) {
        return Gunsmith
                .getGunInfo(stack)
                .flatMap(gi -> Optional
                        .ofNullable(EnergyWeaponBehavior.DATA_MAP.get(gi.gunId()))
                        .map(ei -> new Runtime(ei, gi))
                );
    }
}

package mod.chloeprime.modtechpoweredarsenal.common.standard.guns;

import com.google.gson.annotations.SerializedName;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.modtechpoweredarsenal.common.api.standard.EnhancedGunData;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record EnergyWeaponData(
        @SerializedName("energy_per_shot")
        int energyPerShot,
        @SerializedName("charge_power")
        int chargePower,
        @SerializedName("needs_reload_on_full_heat")
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
                .flatMap(gi -> ((EnhancedGunData) gi.index().getGunData())
                        .getEnergyWeaponData()
                        .map(ei -> new Runtime(ei, gi))
                );
    }
}

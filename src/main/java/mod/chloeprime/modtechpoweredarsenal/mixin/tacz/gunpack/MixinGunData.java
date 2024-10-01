package mod.chloeprime.modtechpoweredarsenal.mixin.tacz.gunpack;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.chloeprime.modtechpoweredarsenal.common.api.standard.EnhancedGunData;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.EnergyWeaponData;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.OverheatData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(GunData.class)
@SuppressWarnings({"AddedMixinMembersNamePattern", "unused"})
public class MixinGunData implements EnhancedGunData {
    @Override
    public Optional<OverheatData> getOverheatData() {
        return Optional.ofNullable(mtpa$overheat);
    }

    @Override
    public Optional<EnergyWeaponData> getEnergyWeaponData() {
        return Optional.ofNullable(mtpa$battery);
    }

    @SerializedName("overheat")
    private @Unique @Nullable OverheatData mtpa$overheat;

    @SerializedName("battery")
    private @Unique @Nullable EnergyWeaponData mtpa$battery;
}

package mod.chloeprime.modtechpoweredarsenal.mixin.tacz.gunpack;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.chloeprime.modtechpoweredarsenal.common.api.standard.EnhancedGunData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

@Mixin(GunData.class)
@SuppressWarnings({"unused"})
public class MixinGunData implements EnhancedGunData {
    @Override
    public Optional<Map<String, Double>> mtpa$getConfiguration() {
        return Optional.ofNullable(mtpa$config);
    }

    @SerializedName("mtpa:config")
    private @Unique @Nullable Map<String, Double> mtpa$config;
}

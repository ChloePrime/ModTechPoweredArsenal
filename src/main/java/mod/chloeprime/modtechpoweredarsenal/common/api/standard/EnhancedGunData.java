package mod.chloeprime.modtechpoweredarsenal.common.api.standard;

import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.EnergyWeaponData;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.OverheatData;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

public interface EnhancedGunData {
    default OptionalDouble getConfiguration(MtpaExtraConfiguration configuration) {
        return mtpa$getConfiguration()
                .map(cfg -> cfg.get(configuration.key()))
                .map(OptionalDouble::of)
                .orElse(OptionalDouble.empty());
    }

    Optional<Map<String, Double>> mtpa$getConfiguration();
    Optional<OverheatData> mtpa$getOverheatData();
    Optional<EnergyWeaponData> mtpa$getEnergyWeaponData();
}

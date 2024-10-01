package mod.chloeprime.modtechpoweredarsenal.common.api.standard;

import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.EnergyWeaponData;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.OverheatData;

import java.util.Optional;

public interface EnhancedGunData {
    Optional<OverheatData> getOverheatData();
    Optional<EnergyWeaponData> getEnergyWeaponData();
}

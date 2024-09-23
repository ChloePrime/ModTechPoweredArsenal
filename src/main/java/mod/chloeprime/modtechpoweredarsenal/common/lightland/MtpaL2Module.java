package mod.chloeprime.modtechpoweredarsenal.common.lightland;

import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.ModLoadStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class MtpaL2Module {
    public static final String ID = "modtech_arsenal_l2";
    public static ResourceLocation loc(String path) {
        return new ResourceLocation(ID, path);
    }

    public static MobEffect getAntiRegenEffect() {
        return ModLoadStatus.L2C_INSTALLED
                ? MtpaL2ModuleImpl.getAntiRegenEffect()
                : MTPA.MobEffects.ANTI_REGEN.get();
    }
}

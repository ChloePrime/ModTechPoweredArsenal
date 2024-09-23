package mod.chloeprime.modtechpoweredarsenal.common.lightland;

import dev.xkmc.l2complements.init.registrate.LCEffects;
import net.minecraft.world.effect.MobEffect;

class MtpaL2ModuleImpl {
    static MobEffect getAntiRegenEffect() {
        return LCEffects.CURSE.get();
    }
}

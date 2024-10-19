package mod.chloeprime.modtechpoweredarsenal.common.lightland.guns;

import dev.xkmc.l2hostility.init.data.LHConfig;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SoulFlamethrowerBehaviorLHProxy {
    public static int getFlameThornDebuffDuration() {
        return LHConfig.COMMON.flameThornTime.get();
    }
}

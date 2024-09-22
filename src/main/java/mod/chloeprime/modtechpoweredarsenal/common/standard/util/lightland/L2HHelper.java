package mod.chloeprime.modtechpoweredarsenal.common.standard.util.lightland;

import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class L2HHelper {
    public static final Capability<MobTraitCap> TRAIT_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static boolean removeTrait(LivingEntity victim, MobTrait trait) {
        return victim.getCapability(TRAIT_CAP).map(cap -> {
            var hasSplit = cap.hasTrait(trait);
            if (hasSplit) {
                cap.removeTrait(trait);
                cap.syncToClient(victim);
                return true;
            } else {
                return false;
            }
        }).orElse(false);
    }
}

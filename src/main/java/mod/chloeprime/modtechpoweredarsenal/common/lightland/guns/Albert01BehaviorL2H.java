package mod.chloeprime.modtechpoweredarsenal.common.lightland.guns;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import dev.xkmc.l2hostility.init.registrate.LHTraits;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.Albert01Behavior;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.lightland.L2HHelper;
import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import mod.chloeprime.modtechpoweredarsenal.network.S2CEnchantedHit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Albert01BehaviorL2H {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGunshotPre(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        if (!(event.getHurtEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (event.getBullet().getPersistentData().getBoolean(Albert01Behavior.PD_KEY)) {
            if (L2HHelper.removeTrait(victim, LHTraits.SPLIT.get())) {
                ModNetwork.sendToNearby(new S2CEnchantedHit(victim.getId()), victim);
            }
        }
    }
}

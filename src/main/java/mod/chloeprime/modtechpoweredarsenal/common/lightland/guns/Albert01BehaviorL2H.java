package mod.chloeprime.modtechpoweredarsenal.common.lightland.guns;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.init.registrate.LHTraits;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.Albert01Behavior;
import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import mod.chloeprime.modtechpoweredarsenal.network.S2CEnchantedHit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Albert01BehaviorL2H {
    public static final Capability<MobTraitCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGunshotPre(EntityHurtByGunEvent.Pre event) {
        if (!(event.getHurtEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (event.getBullet().getPersistentData().getBoolean(Albert01Behavior.PD_KEY)) {
            victim.getCapability(CAPABILITY).ifPresent(cap -> {
                var target = LHTraits.SPLIT.get();
                var hasSplit = cap.hasTrait(target);
                if (hasSplit) {
                    cap.removeTrait(target);
                    cap.syncToClient(victim);
                    ModNetwork.sendToNearby(new S2CEnchantedHit(victim.getId()), victim);
                }
            });
        }
    }
}

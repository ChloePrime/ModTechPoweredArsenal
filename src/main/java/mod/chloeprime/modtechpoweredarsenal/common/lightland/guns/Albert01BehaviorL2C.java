package mod.chloeprime.modtechpoweredarsenal.common.lightland.guns;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import dev.xkmc.l2complements.init.registrate.LCEffects;
import mod.chloeprime.modtechpoweredarsenal.common.standard.guns.Albert01Behavior;
import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import mod.chloeprime.modtechpoweredarsenal.network.S2CEnchantedHit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Albert01BehaviorL2C {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGunshotPre(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        if (!(event.getHurtEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (event.getBullet().getPersistentData().getBoolean(Albert01Behavior.PD_KEY)) {
            var curse = LCEffects.CURSE.get();
            if (!victim.hasEffect(curse)) {
                ModNetwork.sendToNearby(new S2CEnchantedHit(victim.getId()), victim);
            }
            var HALF_MINUTE = 20 * 30;
            victim.addEffect(new MobEffectInstance(curse, HALF_MINUTE));
        }
    }
}

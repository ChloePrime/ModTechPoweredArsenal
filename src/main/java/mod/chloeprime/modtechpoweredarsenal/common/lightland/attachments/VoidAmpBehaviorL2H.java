package mod.chloeprime.modtechpoweredarsenal.common.lightland.attachments;

import dev.xkmc.l2hostility.content.traits.common.AdaptingTrait;
import dev.xkmc.l2hostility.init.registrate.LHTraits;
import mod.chloeprime.modtechpoweredarsenal.common.standard.attachments.VoidAmpBehavior;
import mod.chloeprime.modtechpoweredarsenal.common.lightland.lightland.L2HHelper;
import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import mod.chloeprime.modtechpoweredarsenal.network.S2CEnchantedHit;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 虚空伤害删除适应记录
 */
public class VoidAmpBehaviorL2H {
    @SubscribeEvent
    public static void onPreAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        } else if (!event.getSource().is(VoidAmpBehavior.VOID_MARK)) {
            return;
        }

        var trait = LHTraits.ADAPTIVE.get();
        var victim = event.getEntity();
        victim.getCapability(L2HHelper.TRAIT_CAP)
                .map(cap -> cap.getOrCreateData(trait.getRegistryName(), AdaptingTrait.Data::new))
                .ifPresent(data -> {
                    var hasAny = !data.adaption.isEmpty() && !data.memory.isEmpty();
                    if (hasAny) {
                        ModNetwork.sendToNearby(new S2CEnchantedHit(victim.getId()), victim);
                        data.adaption.clear();
                        data.memory.clear();
                    }
                });
    }
}

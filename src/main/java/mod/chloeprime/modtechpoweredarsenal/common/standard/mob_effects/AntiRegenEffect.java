package mod.chloeprime.modtechpoweredarsenal.common.standard.mob_effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHealEvent;

public class AntiRegenEffect extends MobEffect {
    public AntiRegenEffect(int pColor) {
        super(MobEffectCategory.HARMFUL, pColor);
        MinecraftForge.EVENT_BUS.addListener(this::onRegen);
    }

    private void onRegen(LivingHealEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        } else if (!event.isCancelable()) {
            return;
        } else if (!event.getEntity().hasEffect(this)) {
            return;
        }
        event.setCanceled(true);
    }
}

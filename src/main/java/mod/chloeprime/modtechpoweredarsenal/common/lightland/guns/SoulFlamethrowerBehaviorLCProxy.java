package mod.chloeprime.modtechpoweredarsenal.common.lightland.guns;

import com.google.common.base.Suppliers;
import dev.xkmc.l2library.base.effects.EffectUtil;
import mod.chloeprime.modtechpoweredarsenal.ModLoadStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class SoulFlamethrowerBehaviorLCProxy {
    private static final Supplier<MobEffect> SOUL_FLAME_DEBUFF = Suppliers.memoize(
            () -> ForgeRegistries.MOB_EFFECTS.getValue(
                    new ResourceLocation("l2complements",  "flame")
            )
    );

    public static void addSoulFlameDebuff(LivingEntity victim, @Nullable Entity shooter) {
        var size = victim.getActiveEffectsMap().size();
        int time = ModLoadStatus.L2H_INSTALLED
                ? SoulFlamethrowerBehaviorLHProxy.getFlameThornDebuffDuration()
                : 5 * 20;
        EffectUtil.addEffect(victim, new MobEffectInstance(SOUL_FLAME_DEBUFF.get(), time, size), EffectUtil.AddReason.FORCE, shooter);
    }
}

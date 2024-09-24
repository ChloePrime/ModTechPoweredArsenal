package mod.chloeprime.modtechpoweredarsenal.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.FangEmitter;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EvokerFangs.class)
public class MixinEvokerFang {
    @ModifyExpressionValue(
            method = "dealDamageTo",
            at = @At(value = "CONSTANT", args = "floatValue=6F")
    )
    private float modifyDamage(float original) {
        return (float) FangEmitter
                .getDamageFor((EvokerFangs) (Object) this)
                .orElse(original);
    }
}

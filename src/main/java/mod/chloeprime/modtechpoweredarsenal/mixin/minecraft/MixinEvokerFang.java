package mod.chloeprime.modtechpoweredarsenal.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mod.chloeprime.modtechpoweredarsenal.common.standard.ammo.ShockwaveGrenadeBehavior;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.FangEmitter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EvokerFangs.class)
public class MixinEvokerFang {
    @WrapOperation(
            method = "dealDamageTo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    )
    private boolean modifyDamage(LivingEntity victim, DamageSource entity, float vanillaAmount, Operation<Boolean> original) {
        var self = (EvokerFangs) (Object) this;
        var amount = (float) FangEmitter
                .getDamageFor(self)
                .orElse(vanillaAmount);
        boolean hurt = original.call(victim, entity, amount);
        ShockwaveGrenadeBehavior.onFangHit(self, victim, hurt);
        return hurt;
    }
}

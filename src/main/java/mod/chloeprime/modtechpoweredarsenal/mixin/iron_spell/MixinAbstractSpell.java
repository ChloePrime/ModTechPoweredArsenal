package mod.chloeprime.modtechpoweredarsenal.mixin.iron_spell;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.HateTransferable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSpell.class)
public class MixinAbstractSpell {
    @WrapOperation(
            remap = false,
            method = "getDamageSource(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Lio/redspace/ironsspellbooks/damage/SpellDamageSource;",
            at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/damage/SpellDamageSource;source(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;)Lio/redspace/ironsspellbooks/damage/SpellDamageSource;"))
    private SpellDamageSource transferAttackerForSpellGrenade(Entity projectile, Entity attacker, AbstractSpell spell, Operation<SpellDamageSource> original) {
        Entity realAttacker = attacker instanceof HateTransferable transferable
                ? transferable.mtpa$getHateOwner()
                : attacker;
        Entity realProjectile;

        if (projectile == attacker) {
            realProjectile = realAttacker;
        } else {
            if (projectile instanceof Projectile ownable) {
                ownable.setOwner(realAttacker);
            }
            realProjectile = projectile;
        }

        return original.call(realProjectile, realAttacker, spell);
    }
}

package mod.chloeprime.modtechpoweredarsenal.mixin.iron_spell;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.registries.RegistryBuilder;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SpellRegistry.class, remap = false)
public class MixinSpellRegistry {
    /**
     * 让 Spell Registry 开启 tag 支持
     */
    @Dynamic
    @ModifyExpressionValue(
            method = "*",
            at = @At(value = "NEW", target = "()Lnet/minecraftforge/registries/RegistryBuilder;"))
    private static RegistryBuilder<AbstractSpell> enableTagSupport(RegistryBuilder<AbstractSpell> original) {
        return original.hasTags();
    }
}

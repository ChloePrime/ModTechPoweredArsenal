package mod.chloeprime.modtechpoweredarsenal.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments.CorePerk;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(EnchantmentHelper.class)
public class PerkEnchantmentLimitMixin {
    @WrapMethod(method = "selectEnchantment")
    private static List<EnchantmentInstance> limitPerkCount(RandomSource pRandom, ItemStack pItemStack, int pLevel, boolean pAllowTreasure, Operation<List<EnchantmentInstance>> original) {
        var result = original.call(pRandom, pItemStack, pLevel, pAllowTreasure);
        CorePerk.Impl.limitPerkCount(pItemStack, result);
        return result;
    }
}

package mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 此类附魔只能用附魔书从 1 级升级到 2 级，
 * 而不支持使用附魔书新增。
 */
@Mod.EventBusSubscriber
public class PerkBase extends Enchantment {
    protected PerkBase(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public int getMinCost(int level) {
        return level <= 1 ? 1 : getMaxCost(level);
    }

    @Override
    public int getMaxCost(int pLevel) {
        return 200;
    }

    @SubscribeEvent
    public static void onAnvilRepair(AnvilUpdateEvent event) {
        var existingPerks = getPerks(event.getLeft()).collect(Collectors.toSet());
        var bookPerks = getPerks(event.getRight());
        if (bookPerks.anyMatch(perk -> !existingPerks.contains(perk))) {
            event.setCanceled(true);
        }
    }

    public static Stream<Enchantment> getPerks(ItemStack stack) {
        var enchantments = stack.getItem() instanceof EnchantedBookItem
                ? EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack))
                : stack.getAllEnchantments();
        return enchantments.keySet().stream().filter(PerkBase.class::isInstance);
    }
}

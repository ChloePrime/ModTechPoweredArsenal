package mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * 有此注解的附魔，每个物品上最多只能拥有累计 2 种
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CorePerk {
    class Impl {
        public static final int MAX_ON_BOOKS = 1;
        public static final int MAX_ON_EQUIPMENTS = 2;

        public static boolean isCorePerk(Object object) {
            return object.getClass().getAnnotation(CorePerk.class) != null;
        }

        public static void limitPerkCount(ItemStack stack, List<EnchantmentInstance> list) {
            var iterator = list.iterator();
            var count = 0;
            var max = stack.is(Items.BOOK) ? MAX_ON_BOOKS : MAX_ON_EQUIPMENTS;
            while (iterator.hasNext()) {
                var ench = iterator.next();
                if (!isCorePerk(ench.enchantment)) {
                    continue;
                }
                count++;
                if (count > max) {
                    iterator.remove();
                }
            }
        }
    }
}

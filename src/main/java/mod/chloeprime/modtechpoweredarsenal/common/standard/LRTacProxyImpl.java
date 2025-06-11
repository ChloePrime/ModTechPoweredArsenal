package mod.chloeprime.modtechpoweredarsenal.common.standard;

import com.google.common.base.Suppliers;
import me.xjqsh.lrtactical.api.item.IThrowable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

class LRTacProxyImpl {
    private static final Supplier<Item> META_THROWABLE = Suppliers.memoize(() -> ForgeRegistries.ITEMS.getValue(new ResourceLocation("lrtactical", "throwable")));
    public static @Nonnull ItemStack createThrowable(ResourceLocation id, int count, @Nullable CompoundTag tag) {
        var item = META_THROWABLE.get();
        if (item == null) {
            return ItemStack.EMPTY;
        }
        var grenade = new ItemStack(item, count);
        if (tag != null) {
            grenade.setTag(tag.copy());
        }
        if (item instanceof IThrowable throwable) {
            throwable.setId(grenade, id);
        }
        return grenade;
    }
}

package mod.chloeprime.modtechpoweredarsenal.common.standard;

import mod.chloeprime.modtechpoweredarsenal.ModLoadStatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LRTacProxy {
    public static @Nonnull ItemStack createThrowable(ResourceLocation id, int count, @Nullable CompoundTag nbt) {
        if (ModLoadStatus.LRTAC_INSTALLED) {
            return LRTacProxyImpl.createThrowable(id, count, nbt);
        } else {
            return ItemStack.EMPTY;
        }
    }
}

package mod.chloeprime.modtechpoweredarsenal.common.standard.util;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamSupportMC {
    public static Stream<Supplier<ItemStack>> of(IItemHandler inventory) {
        if (inventory instanceof IItemHandlerModifiable mutable) {
            return of(mutable).map(Function.identity());
        }
        return IntStream
                .range(0, inventory.getSlots())
                .mapToObj(index -> () -> inventory.getStackInSlot(index));
    }

    public static Stream<Property<ItemStack>> of(IItemHandlerModifiable inventory) {
        return IntStream
                .range(0, inventory.getSlots())
                .mapToObj(index -> new ItemSlot(inventory, index));
    }

    private record ItemSlot(IItemHandlerModifiable inventory, int index) implements Property<ItemStack> {
        @Override
        public ItemStack get() {
            return inventory.getStackInSlot(index);
        }

        @Override
        public void set(ItemStack value) {
            inventory.setStackInSlot(index, value);
        }
    }
}

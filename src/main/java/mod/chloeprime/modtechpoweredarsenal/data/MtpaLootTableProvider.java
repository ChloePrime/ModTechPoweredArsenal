package mod.chloeprime.modtechpoweredarsenal.data;

import com.google.common.collect.Iterables;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MtpaLootTableProvider {
    @ParametersAreNonnullByDefault
    public static LootTableProvider create(PackOutput pOutput) {
        return new LootTableProvider(pOutput, Set.of(), List.of(new LootTableProvider.SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK))) {
            @Override
            protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationcontext) {
            }
        };
    }

    public static class Blocks extends BlockLootSubProvider {
        public Blocks() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            dropSelf(MTPA.Blocks.GALLIUM_BLOCK.get());
            dropSelf(MTPA.Blocks.RAW_GALLIUM_BLOCK.get());
            ore(MTPA.Blocks.GALLIUM_ORE, MTPA.Items.RAW_GALLIUM);
            ore(MTPA.Blocks.DEEPSLATE_GALLIUM_ORE, MTPA.Items.RAW_GALLIUM);
        }

        @SuppressWarnings("SameParameterValue")
        private void ore(Supplier<Block> block, Supplier<Item> product) {
            add(block.get(), b -> this.createOreDrop(b, product.get()));
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            return Iterables.transform(MTPA.Blocks.REGISTRY.getEntries(), RegistryObject::get);
        }
    }
}

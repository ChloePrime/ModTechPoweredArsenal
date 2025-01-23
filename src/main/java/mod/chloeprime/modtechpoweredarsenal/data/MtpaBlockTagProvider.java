package mod.chloeprime.modtechpoweredarsenal.data;

import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static mod.chloeprime.modtechpoweredarsenal.MTPA.Blocks.*;

public class MtpaBlockTagProvider extends BlockTagsProvider {
    public MtpaBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ModTechPoweredArsenal.MODID, existingFileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // Ore
        tag(Tags.ORES_GALLIUM).add(GALLIUM_ORE.get(), DEEPSLATE_GALLIUM_ORE.get());
        tag(Blocks.ORES).addTags(Tags.ORES_GALLIUM);
        tag(Blocks.ORE_RATES_SINGULAR).add(GALLIUM_ORE.get(), DEEPSLATE_GALLIUM_ORE.get());
        tag(Blocks.ORES_IN_GROUND_STONE).add(GALLIUM_ORE.get());
        tag(Blocks.ORES_IN_GROUND_DEEPSLATE).add(DEEPSLATE_GALLIUM_ORE.get());

        // Storage Blocks
        tag(Tags.STORAGE_BLOCKS_GALLIUM).add(GALLIUM_BLOCK.get());
        tag(Tags.STORAGE_BLOCKS_RAW_GALLIUM).add(RAW_GALLIUM_BLOCK.get());
        tag(Blocks.STORAGE_BLOCKS).addTags(Tags.STORAGE_BLOCKS_GALLIUM, Tags.STORAGE_BLOCKS_RAW_GALLIUM);
        tag(BlockTags.BEACON_BASE_BLOCKS).add(GALLIUM_BLOCK.get());

        // Mineable
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(GALLIUM_ORE.get(), DEEPSLATE_GALLIUM_ORE.get(), RAW_GALLIUM_BLOCK.get(), GALLIUM_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(GALLIUM_ORE.get(), DEEPSLATE_GALLIUM_ORE.get(), RAW_GALLIUM_BLOCK.get());
    }
}

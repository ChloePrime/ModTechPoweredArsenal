package mod.chloeprime.modtechpoweredarsenal.data;

import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static mod.chloeprime.modtechpoweredarsenal.MTPA.*;
import static mod.chloeprime.modtechpoweredarsenal.MTPA.Items.*;
import static net.minecraftforge.common.Tags.Items;

public class MtpaItemTagProvider extends ItemTagsProvider {
    public MtpaItemTagProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, ModTechPoweredArsenal.MODID, existingFileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        // Ore
        {
            copy(Blocks.Tags.ORES_GALLIUM, Tags.ORES_GALLIUM);
            tag(Items.ORES).addTag(Tags.ORES_GALLIUM);
            tag(Items.ORE_RATES_SINGULAR).add(GALLIUM_ORE.get(), DEEPSLATE_GALLIUM_ORE.get());
            tag(Items.ORES_IN_GROUND_STONE).add(GALLIUM_ORE.get());
            tag(Items.ORES_IN_GROUND_DEEPSLATE).add(DEEPSLATE_GALLIUM_ORE.get());
        }
        // Storage Blocks
        {
            copy(Blocks.Tags.STORAGE_BLOCKS_GALLIUM, Tags.STORAGE_BLOCKS_GALLIUM);
            copy(Blocks.Tags.STORAGE_BLOCKS_RAW_GALLIUM, Tags.STORAGE_BLOCKS_RAW_GALLIUM);
            tag(Items.STORAGE_BLOCKS).addTags(Tags.STORAGE_BLOCKS_GALLIUM, Tags.STORAGE_BLOCKS_RAW_GALLIUM);
        }
        // Ingot
        {
            tag(Tags.INGOTS_GALLIUM).add(GALLIUM_INGOT.get());
            tag(Tags.NUGGETS_GALLIUM).add(GALLIUM_NUGGET.get());
            tag(Items.INGOTS).addTag(Tags.INGOTS_GALLIUM);
            tag(Items.NUGGETS).addTag(Tags.NUGGETS_GALLIUM);
            tag(ItemTags.TRIM_MATERIALS).add(GALLIUM_INGOT.get());
            tag(ItemTags.BEACON_PAYMENT_ITEMS).add(GALLIUM_INGOT.get());

            tag(Tags.GEMS_GALLIUM_NITRIDE).add(GALLIUM_NITRIDE_GEM.get());
            tag(Items.GEMS).addTag(Tags.GEMS_GALLIUM_NITRIDE);
            tag(ItemTags.TRIM_MATERIALS).add(GALLIUM_NITRIDE_GEM.get());
            tag(ItemTags.BEACON_PAYMENT_ITEMS).add(GALLIUM_NITRIDE_GEM.get());
        }
        // Raw Material
        {
            tag(Tags.RAW_MATERIALS_GALLIUM).add(RAW_GALLIUM.get());
            tag(Items.RAW_MATERIALS).addTag(Tags.RAW_MATERIALS_GALLIUM);
        }

        // Misc
        {
            tag(Tags.GUNPOWDER_SMOKELESS).add(SMOKELESS_GUNPOWDER.get());
            tag(Items.GUNPOWDER).addTags(Tags.GUNPOWDER_SMOKELESS);
        }
    }
}

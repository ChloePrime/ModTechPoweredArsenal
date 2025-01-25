package mod.chloeprime.modtechpoweredarsenal.data;

import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;
import java.util.function.Supplier;

public class MtpaItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation GENERATED_ITEM_MODEL = new ResourceLocation("item/generated");

    public MtpaItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ModTechPoweredArsenal.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleTexture(MTPA.Items.GALLIUM_INGOT);
        simpleTexture(MTPA.Items.GALLIUM_NUGGET);
        simpleTexture(MTPA.Items.RAW_GALLIUM);
        simpleTexture(MTPA.Items.GALLIUM_NITRIDE_GEM);
    }

    @SuppressWarnings("deprecation")
    private void simpleTexture(Supplier<Item> item) {
        var id = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.get()));
        singleTexture(id.toString(), GENERATED_ITEM_MODEL, "layer0", new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
    }
}

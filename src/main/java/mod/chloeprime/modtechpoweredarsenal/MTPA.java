package mod.chloeprime.modtechpoweredarsenal;

import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.IfModLoadIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public final class MTPA {
    public static final class Items {
        static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModTechPoweredArsenal.MODID);
        public static final RegistryObject<Item> ANTI_MAGIC_COMPOUND = REGISTRY.register(
                "anti_magic_compound", () -> new Item(new Item.Properties())
        );

        private Items() {}
    }

    static ResourceLocation loc(String path) {
        return ModTechPoweredArsenal.loc(path);
    }

    static void registerIngredientSerializers(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(loc("if_mod_loaded"), IfModLoadIngredient.Serializer.INSTANCE);
        }
    }

    static ItemStack attachment(String path) {
        return AttachmentItemBuilder.create().setId(loc(path)).build();
    }

    private MTPA() {}
}

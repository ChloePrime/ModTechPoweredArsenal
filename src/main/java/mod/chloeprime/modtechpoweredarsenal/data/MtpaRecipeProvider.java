package mod.chloeprime.modtechpoweredarsenal.data;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.DataIngredient;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MtpaRecipeProvider extends RecipeProvider {
    public MtpaRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        material(writer, MTPA.Items.GALLIUM_INGOT, MTPA.Items.GALLIUM_BLOCK, MTPA.Items.GALLIUM_NUGGET);
        storageBlock(writer, MTPA.Items.RAW_GALLIUM, MTPA.Items.RAW_GALLIUM_BLOCK);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MTPA.Items.SMOKELESS_GUNPOWDER.get(), 4)
                .requires(MTPA.Items.HNO3.get())
                .requires(ItemTags.WOOL)
                .unlockedBy("has_hno3", has(MTPA.Items.HNO3.get()))
                .save(writer);
    }

    @SuppressWarnings("SameParameterValue")
    private void material(Consumer<FinishedRecipe> writer, Supplier<? extends Item> ingot, Supplier<? extends Item> block, Supplier<? extends Item> nugget) {
        storageBlock(writer, ingot, block);
        nugget(writer, ingot, nugget);
    }

    public static void buildRecipesWithRegistrate(RegistrateRecipeProvider provider) {
        provider.smelting(DataIngredient.items(MTPA.Items.GALLIUM_ORE.get()), RecipeCategory.MISC, MTPA.Items.GALLIUM_INGOT, 1);
        provider.blasting(DataIngredient.items(MTPA.Items.GALLIUM_ORE.get()), RecipeCategory.MISC, MTPA.Items.GALLIUM_INGOT, 1);
        provider.smelting(DataIngredient.items(MTPA.Items.DEEPSLATE_GALLIUM_ORE.get()), RecipeCategory.MISC, MTPA.Items.GALLIUM_INGOT, 1);
        provider.blasting(DataIngredient.items(MTPA.Items.DEEPSLATE_GALLIUM_ORE.get()), RecipeCategory.MISC, MTPA.Items.GALLIUM_INGOT, 1);
        provider.smelting(DataIngredient.items(MTPA.Items.RAW_GALLIUM.get()), RecipeCategory.MISC, MTPA.Items.GALLIUM_INGOT, 1);
        provider.blasting(DataIngredient.items(MTPA.Items.RAW_GALLIUM.get()), RecipeCategory.MISC, MTPA.Items.GALLIUM_INGOT, 1);
        ItemStack strength2Potion = PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.STRONG_STRENGTH);
        provider.smelting(
                DataIngredient.ingredient(PartialNBTIngredient.of(Items.POTION, Objects.requireNonNull(strength2Potion.getTag())), Items.POTION),
                RecipeCategory.MISC, MTPA.Items.HNO3, 1);
    }

    @SuppressWarnings("deprecation")
    private void storageBlock(Consumer<FinishedRecipe> writer, Supplier<? extends Item> ingot, Supplier<? extends Item> block) {
        var ingotId = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(ingot.get()));
        var blockId = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(block.get()));
        var modid = ingotId.getNamespace();
        nineBlockStorageRecipes(writer,
                RecipeCategory.MISC, ingot.get(), RecipeCategory.BUILDING_BLOCKS, block.get(),
                blockId.toString(), blockId.getPath(), new ResourceLocation(modid, "%s_from_%s".formatted(ingotId.getPath(), blockId.getPath())).toString(), ingotId.getPath());
    }

    @SuppressWarnings("deprecation")
    private void nugget(Consumer<FinishedRecipe> writer, Supplier<? extends Item> ingot, Supplier<? extends Item> nugget) {
        var ingotId = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(ingot.get()));
        var nuggetId = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(nugget.get()));
        var modid = ingotId.getNamespace();
        nineBlockStorageRecipes(writer,
                RecipeCategory.MISC, nugget.get(), RecipeCategory.MISC, ingot.get(),
                new ResourceLocation(modid, "%s_from_nuggets".formatted(ingotId.getPath())).toString(), ingotId.getPath(), nuggetId.toString(), nuggetId.getPath());
    }
}

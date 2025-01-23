package mod.chloeprime.modtechpoweredarsenal.common.standard;

import mod.chloeprime.modtechpoweredarsenal.MTPA;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

public class SpecialRecipes {
    public static void init() {
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(MTPA.Items.Tags.INGOTS_GALLIUM),
                Ingredient.of(Tags.Items.GUNPOWDER),
                MTPA.Items.GALLIUM_NITRIDE_INGOT.get().getDefaultInstance()
        );
    }
}

package mod.chloeprime.modtechpoweredarsenal.common.standard.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class IfModLoadIngredient extends AbstractIngredient {
    private final String modid;
    private final Ingredient whenTrue;
    private final Ingredient whenFalse;
    private final boolean evalResult;

    public IfModLoadIngredient(String modid, Ingredient whenTrue, Ingredient whenFalse) {
        this(modid, ModList.get().isLoaded(modid), whenTrue, whenFalse);
    }

    protected IfModLoadIngredient(String modid, boolean result, Ingredient whenTrue, Ingredient whenFalse) {
        this.modid = modid;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
        this.evalResult = result;
    }

    public Ingredient resolve() {
        return evalResult ? whenTrue : whenFalse;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return resolve().test(stack);
    }

    @Override
    public @Nonnull IntList getStackingIds() {
        return resolve().getStackingIds();
    }

    @Override
    public boolean isEmpty() {
        return resolve().isEmpty();
    }

    @Override
    public boolean isSimple() {
        return resolve().isSimple();
    }

    @Override
    public @Nonnull ItemStack[] getItems() {
        return resolve().getItems();
    }

    @Override
    public @Nonnull IIngredientSerializer<IfModLoadIngredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @Nonnull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("modid", modid);
        json.add("when_true", whenTrue.toJson());
        json.add("when_false", whenFalse.toJson());
        return json;
    }


    @SuppressWarnings("NullableProblems")
    public static class Serializer implements IIngredientSerializer<IfModLoadIngredient>
    {
        public static final IIngredientSerializer<IfModLoadIngredient> INSTANCE = new IfModLoadIngredient.Serializer();

        @Override
        public IfModLoadIngredient parse(JsonObject json)
        {
            var $if = GsonHelper.getAsString(json, "modid");
            var whenTrue = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "when_true"));
            var whenFalse = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "when_false"));
            return new IfModLoadIngredient($if, whenTrue, whenFalse);
        }

        @Override
        public IfModLoadIngredient parse(FriendlyByteBuf buffer)
        {
            var modid = buffer.readUtf();
            var result = buffer.readBoolean();
            var whenTrue = Ingredient.fromNetwork(buffer);
            var whenFalse = Ingredient.fromNetwork(buffer);
            return new IfModLoadIngredient(modid, result, whenTrue, whenFalse);
        }

        @Override
        public void write(FriendlyByteBuf buffer, IfModLoadIngredient ingredient)
        {
            buffer.writeUtf(ingredient.modid);
            buffer.writeBoolean(ingredient.evalResult);
            ingredient.whenTrue.toNetwork(buffer);
            ingredient.whenFalse.toNetwork(buffer);
        }
    }
}

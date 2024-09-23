package mod.chloeprime.modtechpoweredarsenal;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import mod.chloeprime.modtechpoweredarsenal.common.standard.mob_effects.AntiRegenEffect;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.IfModLoadIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;

import static net.minecraft.world.item.Items.BARRIER;

public final class MTPA {
    public static final class Items {
        static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModTechPoweredArsenal.MODID);
        public static final RegistryObject<Item> ANTI_MAGIC_COMPOUND = REGISTRY.register(
                "anti_magic_compound", () -> new Item(new Item.Properties())
        );

        private Items() {}
    }

    public static final class MobEffects {
        static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ModTechPoweredArsenal.MODID);
        public static final RegistryObject<MobEffect> ANTI_REGEN = REGISTRY.register(
                "anti_regen", () -> new AntiRegenEffect(new Color(0x60, 0, 0, 1).getRGB())
        );

        private MobEffects() {}
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

    static ItemStack gun(String namespace, String path) {
        var loc = new ResourceLocation(namespace, path);
        return TimelessAPI.getCommonGunIndex(loc).map(index -> {
            var mode = index.getGunData().getFireModeSet().get(0);
            var builder = GunItemBuilder.create()
                    .setId(loc)
                    .setAmmoCount(index.getGunData().getAmmoAmount())
                    .setFireMode(mode);
            // 上膛
            if (index.getGunData().getBolt() == Bolt.CLOSED_BOLT) {
                builder.setAmmoInBarrel(true);
            }
            return builder.build();
        }).orElseGet(() -> BARRIER.getDefaultInstance().setHoverName(Component.literal("Unknown Weapon")));
    }

    static ItemStack gun(String path) {
        return gun(loc(path).getNamespace(), path);
    }


    static ItemStack ammo(String namespace, String path) {
        var loc = new ResourceLocation(namespace, path);
        return AmmoItemBuilder.create().setId(loc).build();
    }

    static ItemStack ammo(String path) {
        return ammo(loc(path).getNamespace(), path);
    }

    static ItemStack attachment(String namespace, String path) {
        var loc = new ResourceLocation(namespace, path);
        return AttachmentItemBuilder.create().setId(loc).build();
    }

    static ItemStack attachment(String path) {
        return AttachmentItemBuilder.create().setId(loc(path)).build();
    }

    private MTPA() {}
}

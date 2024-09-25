package mod.chloeprime.modtechpoweredarsenal;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments.MicroMissileEnchantment;
import mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments.PrimeChamberPerk;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.FangEmitter;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.Shockwave;
import mod.chloeprime.modtechpoweredarsenal.common.standard.mob_effects.AntiRegenEffect;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.IfModLoadIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;
import java.util.function.Supplier;

import static net.minecraft.world.item.Items.BARRIER;

public final class MTPA {
    public static final class Items {
        static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModTechPoweredArsenal.MODID);
        public static final RegistryObject<Item> ANTI_MAGIC_COMPOUND = REGISTRY.register(
                "anti_magic_compound", () -> new Item(new Item.Properties())
        );

        private Items() {}
    }

    public static final class Entities {
        public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ModTechPoweredArsenal.MODID);
        public static final RegistryObject<EntityType<FangEmitter>> FANG_EMITTER = registerEntity(
                "fang_emitter",
                () -> EntityType.Builder.<FangEmitter>of(FangEmitter::new, MobCategory.MISC)
                        .sized(1F / 16, 1F / 16)
                        .clientTrackingRange(8)
                        .fireImmune()
                        .noSave()
        );

        public static final RegistryObject<EntityType<Shockwave>> SHOCKWAVE = registerEntity(
                "shockwave",
                () -> EntityType.Builder.<Shockwave>of(Shockwave::new, MobCategory.MISC)
                        .sized(0.5F, 1F / 16)
                        .clientTrackingRange(8)
                        .fireImmune()
                        .noSave()
        );
    }

    public static final class Enchantments {
        static final DeferredRegister<Enchantment> REGISTRY = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ModTechPoweredArsenal.MODID);
        public static final EnchantmentCategory GUN_PERKS = EnchantmentCategory.create("MTPA_GUN_PERKS", IGun.class::isInstance);
        public static final RegistryObject<Enchantment> PRIME_CHAMBER = REGISTRY.register("prime_chamber", PrimeChamberPerk::create);
        public static final RegistryObject<Enchantment> MICRO_MISSILE = REGISTRY.register("micro_missile", MicroMissileEnchantment::create);
        private Enchantments() {}
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

    static void registerIngredientSerializers(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(loc("if_mod_loaded"), IfModLoadIngredient.Serializer.INSTANCE);
        }
    }

    static <T extends Entity> RegistryObject<EntityType<T>> registerEntity(String path, Supplier<EntityType.Builder<T>> builder) {
        return Entities.REGISTRY.register(path, () -> builder.get().build(loc(path).toString()));
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

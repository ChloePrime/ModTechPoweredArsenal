package mod.chloeprime.modtechpoweredarsenal;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tterrag.registrate.Registrate;
import mod.chloeprime.modtechpoweredarsenal.common.standard.enchantments.*;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.FangEmitter;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.Shockwave;
import mod.chloeprime.modtechpoweredarsenal.common.standard.mob_effects.AntiRegenEffect;
import mod.chloeprime.modtechpoweredarsenal.common.standard.mob_effects.RecombinationBuffEffect;
import mod.chloeprime.modtechpoweredarsenal.common.standard.util.IfModLoadIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;
import java.util.function.Supplier;

import static net.minecraft.world.item.Items.BARRIER;

public final class MTPA {
    /**
     * Used for recipe generating only
     */
    public static final Registrate REGISTRATE = Registrate.create(ModTechPoweredArsenal.MODID);

    public static final class Blocks {
        public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ModTechPoweredArsenal.MODID);

        public static final RegistryObject<Block> GALLIUM_ORE = REGISTRY.register("gallium_ore", () -> new DropExperienceBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 3.0F)));
        public static final RegistryObject<Block> DEEPSLATE_GALLIUM_ORE = REGISTRY.register("deepslate_gallium_ore", () ->  new DropExperienceBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(4.5F, 3.0F)
                .sound(SoundType.DEEPSLATE)));
        public static final RegistryObject<Block> GALLIUM_BLOCK = REGISTRY.register("gallium_block", () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                .strength(0.2F)
                .sound(SoundType.METAL)));
        public static final RegistryObject<Block> RAW_GALLIUM_BLOCK = REGISTRY.register("raw_gallium_block", () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GOLD)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)));

        public static final class Tags {
            public static final TagKey<Block> ORES_GALLIUM = forgeTag("ores/gallium");
            public static final TagKey<Block> STORAGE_BLOCKS_GALLIUM = forgeTag("storage_blocks/gallium");
            public static final TagKey<Block> STORAGE_BLOCKS_RAW_GALLIUM = forgeTag("storage_blocks/raw_gallium");

            private static TagKey<Block> forgeTag(String path) {
                return BlockTags.create(new ResourceLocation("forge", path));
            }
        }
    }

    public static final class Items {
        static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModTechPoweredArsenal.MODID);
        public static final RegistryObject<Item> ANTI_MAGIC_COMPOUND = REGISTRY.register(
                "anti_magic_compound", () -> new Item(new Item.Properties())
        );
        public static final RegistryObject<BlockItem> GALLIUM_ORE = registerBlockItem("gallium_ore", Blocks.GALLIUM_ORE);
        public static final RegistryObject<BlockItem> DEEPSLATE_GALLIUM_ORE = registerBlockItem("deepslate_gallium_ore", Blocks.DEEPSLATE_GALLIUM_ORE);
        public static final RegistryObject<BlockItem> GALLIUM_BLOCK = registerBlockItem("gallium_block", Blocks.GALLIUM_BLOCK);
        public static final RegistryObject<BlockItem> RAW_GALLIUM_BLOCK = registerBlockItem("raw_gallium_block", Blocks.RAW_GALLIUM_BLOCK);
        public static final RegistryObject<Item> RAW_GALLIUM = registerSimpleItem("raw_gallium");
        public static final RegistryObject<Item> GALLIUM_INGOT = registerSimpleItem("gallium_ingot");
        public static final RegistryObject<Item> GALLIUM_NUGGET = registerSimpleItem("gallium_nugget");

        public static final RegistryObject<Item> GALLIUM_NITRIDE_INGOT = registerSimpleItem("gallium_nitride_ingot");

        private Items() {}

        public static final class Tags {
            public static final TagKey<Item> INGOTS_GALLIUM = forgeTag("ingots/gallium");
            public static final TagKey<Item> NUGGETS_GALLIUM = forgeTag("nuggets/gallium");
            public static final TagKey<Item> ORES_GALLIUM = forgeTag("ores/gallium");
            public static final TagKey<Item> RAW_MATERIALS_GALLIUM = forgeTag("raw_materials/gallium");
            public static final TagKey<Item> STORAGE_BLOCKS_GALLIUM = forgeTag("storage_blocks/gallium");
            public static final TagKey<Item> STORAGE_BLOCKS_RAW_GALLIUM = forgeTag("storage_blocks/raw_gallium");

            public static final TagKey<Item> INGOTS_GALLIUM_NITRIDE = forgeTag("ingots/gallium_nitride");

            private static TagKey<Item> forgeTag(String path) {
                return ItemTags.create(new ResourceLocation("forge", path));
            }
        }
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

    @SuppressWarnings("unused")
    public static final class Enchantments {
        static final DeferredRegister<Enchantment> REGISTRY = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ModTechPoweredArsenal.MODID);
        public static final EnchantmentCategory GUN_PERKS = EnchantmentCategory.create("MTPA_GUN_PERKS", IGun.class::isInstance);
        public static final RegistryObject<Enchantment> PRIME_CHAMBER = REGISTRY.register("prime_chamber", PrimeChamberPerk::create);
        public static final RegistryObject<Enchantment> MICRO_MISSILE = REGISTRY.register("micro_missile", MicroMissileEnchantment::create);
        public static final RegistryObject<Enchantment> SUBSISTENCE = REGISTRY.register("subsistence", SubsistencePerk::create);
        public static final RegistryObject<Enchantment> RECONSTRUCTION = REGISTRY.register("reconstruction", ReconstructionPerk::create);
        public static final RegistryObject<Enchantment> RECOMBINATION = REGISTRY.register("recombination", RecombinationPerk::create);
        public static final RegistryObject<Enchantment> RANGER = REGISTRY.register("ranger", RangerPerk::create);
        private Enchantments() {}
    }

    public static final class MobEffects {
        static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ModTechPoweredArsenal.MODID);
        public static final RegistryObject<MobEffect> ANTI_REGEN = REGISTRY.register(
                "anti_regen", () -> new AntiRegenEffect(new Color(0x60, 0, 0, 1).getRGB())
        );
        public static final RegistryObject<MobEffect> RECOMBINATION_BUFF = REGISTRY.register(
                "recombination_buff", () -> new RecombinationBuffEffect(0)
        );

        private MobEffects() {}
    }

    public static final class Sounds {
        static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModTechPoweredArsenal.MODID);
        public static final RegistryObject<SoundEvent> GUN_INSTRUMENT = registerSound("gun_instrument");
    }

    static RegistryObject<Item> registerSimpleItem(String path) {
        return Items.REGISTRY.register(path, () -> new Item(new Item.Properties()));
    }

    static RegistryObject<BlockItem> registerBlockItem(String path, Supplier<Block> block) {
        return Items.REGISTRY.register(path, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    static RegistryObject<SoundEvent> registerSound(String path) {
        return Sounds.REGISTRY.register(path, () -> SoundEvent.createVariableRangeEvent(loc(path)));
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

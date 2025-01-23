package mod.chloeprime.modtechpoweredarsenal;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import mod.chloeprime.modtechpoweredarsenal.common.standard.SpecialRecipes;
import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(ModTechPoweredArsenal.MODID)
public final class ModTechPoweredArsenal {
    public static final String MODID = "modtech_arsenal";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static final Supplier<ItemStack> CREATIVE_TAB_ICON = Suppliers.memoize(
            () -> MTPA.attachment("ammo_mod_antimagic")
    );

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.%s.main".formatted(MODID)))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(CREATIVE_TAB_ICON)
            .displayItems(MTPA::buildCreativeTabContents).build());

    public ModTechPoweredArsenal() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        registerDFRs(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerDFRs(IEventBus bus) {
        MTPA.Blocks.REGISTRY.register(bus);
        MTPA.Items.REGISTRY.register(bus);
        MTPA.Entities.REGISTRY.register(bus);
        MTPA.Enchantments.REGISTRY.register(bus);
        MTPA.MobEffects.REGISTRY.register(bus);
        MTPA.Sounds.REGISTRY.register(bus);
        CREATIVE_MODE_TABS.register(bus);
        bus.addListener(MTPA::registerIngredientSerializers);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::init);
        event.enqueueWork(SpecialRecipes::init);
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }
}

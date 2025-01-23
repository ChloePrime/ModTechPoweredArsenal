package mod.chloeprime.modtechpoweredarsenal.data;

import com.tterrag.registrate.providers.ProviderType;
import mod.chloeprime.modtechpoweredarsenal.MTPA;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MtpaDatagen {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();

        var blockTagProvider = new MtpaBlockTagProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTagProvider);
        generator.addProvider(event.includeServer(), new MtpaItemTagProvider(generator.getPackOutput(), event.getLookupProvider(), blockTagProvider.contentsGetter(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), MtpaLootTableProvider.create(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new MtpaRecipeProvider(generator.getPackOutput()));
        MTPA.REGISTRATE.addDataGenerator(ProviderType.RECIPE, MtpaRecipeProvider::buildRecipesWithRegistrate);

        generator.addProvider(event.includeClient(), new MtpaBlockStateProvider(generator.getPackOutput(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new MtpaItemModelProvider(generator.getPackOutput(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new MtpaSoundProvider(generator.getPackOutput(), event.getExistingFileHelper()));
    }
}

package mod.chloeprime.modtechpoweredarsenal.client;

import mod.chloeprime.modtechpoweredarsenal.MTPA;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MtpaClient {
    @SubscribeEvent
    public static void onRegRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MTPA.Entities.FANG_EMITTER.get(), NoopRenderer::new);
    }
}

package mod.chloeprime.modtechpoweredarsenal.client.iron_spell.throwable;

import me.xjqsh.lrtactical.client.renderer.entity.ThrowableEntityRenderer;
import mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable.IronSpellGrenade;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class IronSpellModuleClient {
    public static void init(IEventBus modbus) {
        modbus.register(IronSpellModuleClient.class);
    }

    @SubscribeEvent
    public static void onRegRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(IronSpellGrenade.TYPE, ThrowableEntityRenderer::new);
    }
}

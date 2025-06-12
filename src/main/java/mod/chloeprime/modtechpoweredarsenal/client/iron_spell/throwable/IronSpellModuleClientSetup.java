package mod.chloeprime.modtechpoweredarsenal.client.iron_spell.throwable;

import me.xjqsh.lrtactical.client.renderer.entity.ThrowableEntityRenderer;
import mod.chloeprime.modtechpoweredarsenal.common.iron_spell.throwable.IronSpellModuleContent;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class IronSpellModuleClientSetup {
    public static void init(IEventBus modbus) {
        modbus.register(IronSpellModuleClientSetup.class);
        MinecraftForge.EVENT_BUS.register(IronSpellGrenadeTooltip.class);
    }

    @SubscribeEvent
    public static void onRegRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(IronSpellModuleContent.VIRTUAL_CASTER.get(), NoopRenderer::new);
        event.registerEntityRenderer(IronSpellModuleContent.IRON_SPELL_GRENADE.get(), ThrowableEntityRenderer::new);
    }
}

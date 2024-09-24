package mod.chloeprime.modtechpoweredarsenal.client;

import mod.chloeprime.modtechpoweredarsenal.MTPA;
import mod.chloeprime.modtechpoweredarsenal.common.standard.entities.Shockwave;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MtpaClient {
    @SubscribeEvent
    public static void onRegRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MTPA.Entities.FANG_EMITTER.get(), NoopRenderer::new);
        event.registerEntityRenderer(MTPA.Entities.SHOCKWAVE.get(), NoopRenderer::new);
    }

    private static final RandomGenerator RNG = new Random();

    public static void onShockwaveClientTick(Shockwave shockwave, Level level) {
        var cx = shockwave.getX();
        var cy = shockwave.getY() + 0.25;
        var cz = shockwave.getZ();
        var dx = shockwave.getSize();
        var dy = 0.25;
        var dz = shockwave.getSize();
        for (int i = 0; i < 4; i++) {
            var x = cx + dx * (RNG.nextFloat() - RNG.nextFloat());
            var y = cy + dy * (RNG.nextFloat() - RNG.nextFloat());
            var z = cz + dz * (RNG.nextFloat() - RNG.nextFloat());
            level.addParticle(
                    ParticleTypes.SWEEP_ATTACK, x, y, z, 0, 0, 0
            );
        }
    }
}

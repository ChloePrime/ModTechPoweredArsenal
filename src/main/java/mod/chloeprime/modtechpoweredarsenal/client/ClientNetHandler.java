package mod.chloeprime.modtechpoweredarsenal.client;

import mod.chloeprime.modtechpoweredarsenal.mixin.minecraft.client.ParticleEngineAccessor;
import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import mod.chloeprime.modtechpoweredarsenal.network.S2CEnchantedHit;
import mod.chloeprime.modtechpoweredarsenal.network.S2CPlasmaHitBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * @see ModNetwork
 */
public class ClientNetHandler {
    public static void handleEnchantedHit(S2CEnchantedHit packet, NetworkEvent.Context ignoredContext) {
        var mc = Minecraft.getInstance();
        Optional.ofNullable(mc.level)
                .map(lvl -> lvl.getEntity(packet.id()))
                .ifPresent(entity -> mc.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT));
    }

    public static void handlePlasmaHitBlock(S2CPlasmaHitBlock packet, NetworkEvent.Context ignoredContext) {
        var player = ConstHolder.MC.player;
        if (player == null) {
            return;
        }
        var pitch = ConstHolder.RNG.nextFloat(0.9F, 1 / 0.9F);
        player.level().playLocalSound(
                packet.pos().x(), packet.pos().y(), packet.pos().z(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                1, pitch, true
        );
        var pos = packet.pos().add(Vec3.atLowerCornerOf(packet.normal().getNormal()).scale(1F / 64));
        ((ParticleEngineAccessor) ConstHolder.MC.particleEngine).getTrackingEmitters().add(new ParticleEmitter(
                player.clientLevel, player,
                pos, ParticleTypes.SMOKE,
                ConstHolder.PLASMA_HOLE_EMITTER_CONFIG
        ));
    }

    private static class ConstHolder {
        private static final Minecraft MC = Minecraft.getInstance();
        private static final RandomGenerator RNG = new Random();
        private static final ParticleEmitter.Config PLASMA_HOLE_EMITTER_CONFIG = new ParticleEmitter.Config(
                60, 1, 3, new Vec3(0, 0.125, 0)
        );
    }
}

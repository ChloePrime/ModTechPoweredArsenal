package mod.chloeprime.modtechpoweredarsenal.client;

import com.tacz.guns.client.particle.BulletHoleParticle;
import mod.chloeprime.modtechpoweredarsenal.common.standard.attachments.PlasmaVisualDispatcher;
import mod.chloeprime.modtechpoweredarsenal.common.standard.internal.MaybePlasmaBulletHoleParticle;
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
        // 播放冒烟音效
        var pitch = ConstHolder.RNG.nextFloat(0.9F, 1 / 0.9F);
        player.level().playLocalSound(
                packet.pos().x(), packet.pos().y(), packet.pos().z(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                1, pitch, false
        );
        // 添加冒烟emitter
        var pos = packet.pos().add(Vec3.atLowerCornerOf(packet.normal().getNormal()).scale(1F / 64));
        ((ParticleEngineAccessor) ConstHolder.MC.particleEngine).getTrackingEmitters().add(new ParticleEmitter(
                player.clientLevel, player,
                pos, ParticleTypes.SMOKE,
                ConstHolder.PLASMA_HOLE_EMITTER_CONFIG
        ));
        // 添加长续航版的弹孔
        var bulletHole = new BulletHoleParticle(
                player.clientLevel,
                packet.pos().x(), packet.pos().y(), packet.pos().z(),
                packet.normal(), packet.support(),
                packet.ammoId().toString(), packet.gunId().toString()
        );
        ((MaybePlasmaBulletHoleParticle) bulletHole).mtpa$setPlasma();
        ConstHolder.MC.particleEngine.add(bulletHole);
    }

    private static class ConstHolder {
        private static final Minecraft MC = Minecraft.getInstance();
        private static final RandomGenerator RNG = new Random();
        private static final ParticleEmitter.Config PLASMA_HOLE_EMITTER_CONFIG = new ParticleEmitter.Config(
                PlasmaVisualDispatcher.BULLET_HOLE_HOT_DURATION, 1, 3, new Vec3(0, 0.125, 0)
        );
    }
}

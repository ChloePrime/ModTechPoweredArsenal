package mod.chloeprime.modtechpoweredarsenal.client;

import mod.chloeprime.modtechpoweredarsenal.network.ModNetwork;
import mod.chloeprime.modtechpoweredarsenal.network.S2CEnchantedHit;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

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
}

package mod.chloeprime.modtechpoweredarsenal.network;

import mod.chloeprime.modtechpoweredarsenal.ModTechPoweredArsenal;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class ModNetwork {
    public static final String VERSION = "1.0.0";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ModTechPoweredArsenal.loc("play_channel"),
            () -> VERSION, VERSION::equals, VERSION::equals
    );

    public static void sendToNearby(Object message, Entity center) {
        sendToNearby(message, () -> center);
    }

    public static void sendToNearby(Object message, Supplier<Entity> center) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(center), message);
    }

    private static final AtomicInteger ID_COUNT = new AtomicInteger(1);

    public static void init() {
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), S2CPlasmaHitBlock.class, S2CPlasmaHitBlock::encode, S2CPlasmaHitBlock::decode, S2CPlasmaHitBlock::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), S2CEnchantedHit.class, S2CEnchantedHit::encode, S2CEnchantedHit::decode, S2CEnchantedHit::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private ModNetwork() {}
}

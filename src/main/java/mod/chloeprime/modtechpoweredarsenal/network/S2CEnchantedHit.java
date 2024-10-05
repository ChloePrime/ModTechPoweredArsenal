package mod.chloeprime.modtechpoweredarsenal.network;

import mod.chloeprime.modtechpoweredarsenal.client.ClientNetHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CEnchantedHit(
        int id
) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
    }

    public static S2CEnchantedHit decode(FriendlyByteBuf buf) {
        var id = buf.readVarInt();
        return new S2CEnchantedHit(id);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientNetHandler.handleEnchantedHit(this, context.get()));
    }
}

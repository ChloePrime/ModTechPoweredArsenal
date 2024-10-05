package mod.chloeprime.modtechpoweredarsenal.network;

import mod.chloeprime.modtechpoweredarsenal.client.ClientNetHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CPlasmaHitBlock(
        Vec3 pos, Direction normal
) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeEnum(normal);
    }

    public static S2CPlasmaHitBlock decode(FriendlyByteBuf buf) {
        var x = buf.readDouble();
        var y = buf.readDouble();
        var z = buf.readDouble();
        var d = buf.readEnum(Direction.class);
        return new S2CPlasmaHitBlock(new Vec3(x, y, z), d);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientNetHandler.handlePlasmaHitBlock(this, context.get()));
        context.get().setPacketHandled(true);
    }
}

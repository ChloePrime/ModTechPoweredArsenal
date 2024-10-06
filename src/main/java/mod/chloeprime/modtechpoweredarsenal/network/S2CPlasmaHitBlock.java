package mod.chloeprime.modtechpoweredarsenal.network;

import mod.chloeprime.modtechpoweredarsenal.client.ClientNetHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CPlasmaHitBlock(
        Vec3 pos,
        Direction normal,
        BlockPos support,
        ResourceLocation ammoId,
        ResourceLocation gunId
) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeEnum(normal);
        buf.writeBlockPos(support);
        buf.writeResourceLocation(ammoId);
        buf.writeResourceLocation(gunId);
    }

    public static S2CPlasmaHitBlock decode(FriendlyByteBuf buf) {
        var x = buf.readDouble();
        var y = buf.readDouble();
        var z = buf.readDouble();
        var normal = buf.readEnum(Direction.class);
        var support = buf.readBlockPos();
        var ammoId = buf.readResourceLocation();
        var gunId = buf.readResourceLocation();
        return new S2CPlasmaHitBlock(new Vec3(x, y, z), normal, support, ammoId, gunId);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientNetHandler.handlePlasmaHitBlock(this, context.get()));
        context.get().setPacketHandled(true);
    }
}

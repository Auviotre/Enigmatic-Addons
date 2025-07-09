package auviotre.enigmatic.addon.packets.server;

import auviotre.enigmatic.addon.packets.clients.PacketClientEtheriumShieldSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketEtheriumShieldSync {
    public int tick;

    public PacketEtheriumShieldSync(int tick) {
        this.tick = tick;
    }

    public static void encode(PacketEtheriumShieldSync message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.tick);
    }

    public static PacketEtheriumShieldSync decode(FriendlyByteBuf buffer) {
        return new PacketEtheriumShieldSync(buffer.readInt());
    }


    public static void handle(PacketEtheriumShieldSync msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PacketClientEtheriumShieldSync.handlePacket(msg, contextSupplier)));
        context.setPacketHandled(true);
    }
}

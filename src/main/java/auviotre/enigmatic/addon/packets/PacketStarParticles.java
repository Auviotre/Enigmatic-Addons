package auviotre.enigmatic.addon.packets;

import auviotre.enigmatic.addon.client.particles.StarDustParticle;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketStarParticles {
    private final double x;
    private final double y;
    private final double z;
    private final int amount;

    public PacketStarParticles(double x, double y, double z, int number) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.amount = number;
    }

    public static void encode(PacketStarParticles msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeInt(msg.amount);
    }

    public static PacketStarParticles decode(FriendlyByteBuf buf) {
        return new PacketStarParticles(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void handle(PacketStarParticles msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            int amount = (int) ((float) msg.amount * SuperpositionHandler.getParticleMultiplier());

            for (int counter = 0; counter < 160; counter++) {
                double theta = Math.random() * 2 * Math.PI;
                double phi = (Math.random() - 0.5D) * Math.PI;
                double dx = Math.cos(theta) * Math.cos(phi) * 0.75D;
                double dy = Math.sin(phi) * 0.75D;
                double dz = Math.sin(theta) * Math.cos(phi) * 0.75D;
                player.level().addParticle(StarDustParticle.get(player.getRandom()), msg.x, msg.y, msg.z, dx, dy, dz);
            }

        });
        ctx.get().setPacketHandled(true);
    }
}
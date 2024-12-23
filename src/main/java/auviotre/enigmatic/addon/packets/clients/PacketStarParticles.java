package auviotre.enigmatic.addon.packets.clients;

import auviotre.enigmatic.addon.client.particles.StarDustParticle;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.AstralBreaker;
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
    private final int mode;

    public PacketStarParticles(double x, double y, double z, int number, int mode) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.amount = number;
        this.mode = mode;
    }

    public static void encode(PacketStarParticles msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeInt(msg.amount);
        buf.writeInt(msg.mode);
    }

    public static PacketStarParticles decode(FriendlyByteBuf buf) {
        return new PacketStarParticles(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt(), buf.readInt());
    }

    public static void handle(PacketStarParticles msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            int amount = (int) ((float) msg.amount * SuperpositionHandler.getParticleMultiplier());
            if (msg.mode == 1 && AstralBreaker.flameParticlesToggle.getValue()) {
                float modifier = switch (Minecraft.getInstance().options.particles().get()) {
                    case MINIMAL -> 0.1F;
                    case DECREASED -> 0.25F;
                    default -> 0.35F;
                };
                amount = (int) ((float) msg.amount * modifier);
                for (int counter = 0; counter <= amount; ++counter) {
                    player.level().addParticle(StarDustParticle.get(player.getRandom()), true, msg.x + (Math.random() - 0.5), msg.y + (Math.random() - 0.5), msg.z + (Math.random() - 0.5), (Math.random() - 0.5) * 0.1, (Math.random() - 0.5) * 0.1, (Math.random() - 0.5) * 0.1);
                }
            } else if (msg.mode == 0) {
                for (int counter = 0; counter < amount; counter++) {
                    double theta = Math.random() * 2 * Math.PI;
                    double phi = (Math.random() - 0.5D) * Math.PI;
                    double dx = Math.cos(theta) * Math.cos(phi) * 0.75D;
                    double dy = Math.sin(phi) * 0.75D;
                    double dz = Math.sin(theta) * Math.cos(phi) * 0.75D;
                    player.level().addParticle(StarDustParticle.get(player.getRandom()), msg.x, msg.y, msg.z, dx, dy, dz);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
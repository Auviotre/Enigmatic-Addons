package auviotre.enigmatic.addon.packets.clients;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketEvilCage {
    private final double x;
    private final double y;
    private final double z;
    private final double width;
    private final double height;
    private final int mode;

    public PacketEvilCage(double x, double y, double z, double width, double height, int mode) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.mode = mode;
    }

    public static void encode(PacketEvilCage msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeDouble(msg.width);
        buf.writeDouble(msg.height);
        buf.writeInt(msg.mode);
    }

    public static PacketEvilCage decode(FriendlyByteBuf buf) {
        return new PacketEvilCage(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void handle(PacketEvilCage msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            Level level = player.level();
            for (double i = -msg.width; i < msg.width; i += 0.2) {
                level.addParticle(ParticleTypes.WITCH, msg.x + i, msg.y + msg.height, msg.z + msg.width, 0, 0, 0);
                level.addParticle(ParticleTypes.WITCH, msg.x + i, msg.y + msg.height, msg.z - msg.width, 0, 0, 0);
                level.addParticle(ParticleTypes.WITCH, msg.x + i, msg.y, msg.z + msg.width, 0, 0, 0);
                level.addParticle(ParticleTypes.WITCH, msg.x + i, msg.y, msg.z - msg.width, 0, 0, 0);
            }
            for (double i = -msg.width; i < msg.width; i += 0.2) {
                level.addParticle(ParticleTypes.WITCH, msg.x + msg.width, msg.y + msg.height, msg.z + i, 0, 0, 0);
                level.addParticle(ParticleTypes.WITCH, msg.x - msg.width, msg.y + msg.height, msg.z + i, 0, 0, 0);
                level.addParticle(ParticleTypes.WITCH, msg.x + msg.width, msg.y, msg.z + i, 0, 0, 0);
                level.addParticle(ParticleTypes.WITCH, msg.x - msg.width, msg.y, msg.z + i, 0, 0, 0);
            }
            for (double i = 0; i < msg.height; i += 0.2) {
                level.addParticle(ParticleTypes.WITCH, msg.x + msg.width, msg.y + i, msg.z + msg.width, 0, 0, 0);
                level.addParticle(ParticleTypes.WITCH, msg.x - msg.width, msg.y + i, msg.z + msg.width, 0, 0, 0);
                level.addParticle(ParticleTypes.WITCH, msg.x + msg.width, msg.y + i, msg.z - msg.width, 0, 0, 0);
                level.addParticle(ParticleTypes.WITCH, msg.x - msg.width, msg.y + i, msg.z - msg.width, 0, 0, 0);
            }
        });
        context.get().setPacketHandled(true);
    }
}
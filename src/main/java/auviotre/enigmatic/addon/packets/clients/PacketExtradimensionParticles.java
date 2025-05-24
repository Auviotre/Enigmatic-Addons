package auviotre.enigmatic.addon.packets.clients;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketExtradimensionParticles {
    private final double x;
    private final double y;
    private final double z;
    private final double width;
    private final double height;
    private final int mode;

    public PacketExtradimensionParticles(double x, double y, double z, double width, double height, int mode) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.mode = mode;
    }

    public static void encode(PacketExtradimensionParticles msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeDouble(msg.width);
        buf.writeDouble(msg.height);
        buf.writeInt(msg.mode);
    }

    public static PacketExtradimensionParticles decode(FriendlyByteBuf buf) {
        return new PacketExtradimensionParticles(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void handle(PacketExtradimensionParticles msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            Level level = player.level();
            if (msg.mode == 1) {
                BlockPos blockPos = new BlockPos(Mth.floor(msg.x), Mth.floor(msg.y), Mth.floor(msg.z));
                double xOffset, yOffset, zOffset;
                for (int i = 0; i < 24 + msg.height * 2 + msg.width * 5; i++) {
                    xOffset = msg.width * (0.5F - player.getRandom().nextFloat());
                    yOffset = msg.height * player.getRandom().nextFloat();
                    zOffset = msg.width * (0.5F - player.getRandom().nextFloat());
                    level.addParticle(ParticleTypes.WITCH, msg.x + xOffset, msg.y + yOffset, msg.z + zOffset, 0, 0, 0);
                    if (i % 2 == 0)
                        level.addParticle(ParticleTypes.CLOUD, msg.x + xOffset, msg.y + yOffset, msg.z + zOffset, 0, 0, 0);
                    if (i % 8 == 0)
                        level.addParticle(ParticleTypes.EXPLOSION, msg.x + xOffset * 2, msg.y + yOffset, msg.z + zOffset * 2, 0, 0, 0);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
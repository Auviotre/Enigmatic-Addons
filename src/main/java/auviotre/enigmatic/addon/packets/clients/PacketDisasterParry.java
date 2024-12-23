package auviotre.enigmatic.addon.packets.clients;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketDisasterParry {
    private final double x;
    private final double y;
    private final double z;
    private final int mode;

    public PacketDisasterParry(double x, double y, double z, int mode) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.mode = mode;
    }

    public static void encode(PacketDisasterParry msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeInt(msg.mode);
    }

    public static PacketDisasterParry decode(FriendlyByteBuf buf) {
        return new PacketDisasterParry(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void handle(PacketDisasterParry msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            int amount = 18;
            if (msg.mode == 0) {
                level.playLocalSound(msg.x, msg.y, msg.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.5F + (float) Math.random() * 0.1F, true);
            } else if (msg.mode == 1) {
                level.playLocalSound(msg.x, msg.y, msg.z, SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F, true);
                level.playLocalSound(msg.x, msg.y, msg.z, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 1.2F, 1.25F, true);
                for (int i = 0; i < amount * 2; i++) {
                    double range = Math.random() * 0.3 + 0.1;
                    level.addParticle(ParticleTypes.SQUID_INK, msg.x, msg.y + 0.2 * Math.random(), msg.z, range * Math.sin(Math.PI / amount * i), 0.0, range * Math.cos(Math.PI / amount * i));
                }
                for (int i = 0; i < amount * 2; i++) {
                    level.addParticle(ParticleTypes.SQUID_INK, msg.x + 3 * Math.sin(Math.PI / amount * i), msg.y + 0.1 * Math.random(), msg.z + 3 * Math.cos(Math.PI / amount * i), 0.0, Math.random() * 0.3, 0.0);
                }
            } else if (msg.mode == 2) {
                for (int i = 0; i < amount * 3; i++) {
                    level.addParticle(ParticleTypes.SQUID_INK, msg.x + 2 * Math.random() - 1, msg.y, msg.z + 2 * Math.random() - 1, 0, Math.random(), 0);
                }
                for (int i = 0; i < amount; i++) {
                    level.addParticle(ParticleTypes.SQUID_INK, msg.x, msg.y + 0.2 * Math.random() + 0.5, msg.z, 0.4 * Math.sin(Math.PI / 2 / amount * i), 0.0, 0.4 * Math.cos(Math.PI / 2 / amount * i));
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
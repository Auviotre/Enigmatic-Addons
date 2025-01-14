package auviotre.enigmatic.addon.packets.clients;

import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketDescendingChaos {
    private final double x;
    private final double y;
    private final double z;

    public PacketDescendingChaos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(PacketDescendingChaos msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    public static PacketDescendingChaos decode(FriendlyByteBuf buf) {
        return new PacketDescendingChaos(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(PacketDescendingChaos msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            int amount = 36;
            level.playLocalSound(msg.x, msg.y, msg.z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.75F, 0.25F, true);
            level.playLocalSound(msg.x, msg.y, msg.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2F, 1.25F, true);
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, msg.x, msg.y, msg.z, 0, 0, 0);
            for (int i = 0; i < amount * 2; i++) {
                double range = Math.random() * 0.3 + 0.1;
                level.addParticle(ParticleTypes.SQUID_INK, msg.x, msg.y + 0.2 * Math.random() + 0.1, msg.z, range * Math.sin(Math.PI / amount * i), 0.0, range * Math.cos(Math.PI / amount * i));
            }
            for (int i = 0; i < amount * 2; i++) {
                double range = Math.random() * 0.3 + 0.1;
                level.addParticle(ParticleTypes.END_ROD, msg.x, msg.y + 0.2 * Math.random() + 0.1, msg.z, range * Math.sin(Math.PI / amount * i), 0.0, range * Math.cos(Math.PI / amount * i));
            }
            for (int i = 0; i < amount * 2; i++) {
                double range = Math.random() * 4 + 1;
                level.addParticle(ParticleTypes.SQUID_INK, msg.x + range * Math.sin(Math.PI / amount * i), msg.y + 0.1 * Math.random(), msg.z + range * Math.cos(Math.PI / amount * i), 0.0, Math.random(), 0.0);
            }
            for (int i = 0; i < amount * 2; i++) {
                double range = Math.random() * 4 + 1;
                level.addParticle(ParticleTypes.END_ROD, msg.x + range * Math.sin(Math.PI / amount * i), msg.y + 0.1 * Math.random(), msg.z + range * Math.cos(Math.PI / amount * i), 0.0, Math.random(), 0.0);
            }
            for (int i = 0; i < amount * 2; i++) {
                double range = Math.random() * 4 + 1;
                level.addParticle(EnigmaticAddonParticles.ABYSS_CHAOS, msg.x + range * Math.sin(Math.PI / amount * i), msg.y + 0.1 * Math.random(), msg.z + range * Math.cos(Math.PI / amount * i), 0.0, Math.random(), 0.0);
            }
        });
        context.get().setPacketHandled(true);
    }
}
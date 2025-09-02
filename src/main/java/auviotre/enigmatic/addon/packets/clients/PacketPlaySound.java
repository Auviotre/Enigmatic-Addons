package auviotre.enigmatic.addon.packets.clients;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class PacketPlaySound {
    private final float volume;
    private final float pitch;
    private final SoundEvent sound;

    public PacketPlaySound(SoundEvent sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static void encode(PacketPlaySound msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.sound.getLocation());
        buf.writeFloat(msg.volume);
        buf.writeFloat(msg.pitch);
    }

    public static PacketPlaySound decode(FriendlyByteBuf buf) {
        SoundEvent value = ForgeRegistries.SOUND_EVENTS.getValue(buf.readResourceLocation());
        return new PacketPlaySound(value, buf.readFloat(), buf.readFloat());
    }

    public static void handle(PacketPlaySound msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft.getInstance().player.playSound(msg.sound, msg.volume, msg.pitch);
        });
        context.get().setPacketHandled(true);
    }
}
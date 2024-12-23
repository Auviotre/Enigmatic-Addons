package auviotre.enigmatic.addon.packets.clients;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketMaliceTotem {
    private final double x;
    private final double y;
    private final double z;

    public PacketMaliceTotem(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(PacketMaliceTotem msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    public static PacketMaliceTotem decode(FriendlyByteBuf buf) {
        return new PacketMaliceTotem(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(PacketMaliceTotem msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft instance = Minecraft.getInstance();
            Player player = EnigmaticLegacy.PROXY.getClientPlayer();
            instance.particleEngine.createTrackingEmitter(player, ParticleTypes.WITCH, 40);
            instance.level.playLocalSound(msg.x, msg.y, msg.z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F, false);
            instance.gameRenderer.displayItemActivation(EnigmaticAddonItems.TOTEM_OF_MALICE.getDefaultInstance());
        });
        context.get().setPacketHandled(true);
    }
}
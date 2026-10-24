package auviotre.enigmatic.addon.packets.clients;

import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketForgerCrystal {
    private final double x, y ,z;
    public final ItemStack totem;

    public PacketForgerCrystal(double x, double y, double z, ItemStack stack) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.totem = stack;
    }

    public static void encode(PacketForgerCrystal msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeItem(msg.totem);
    }

    public static PacketForgerCrystal decode(FriendlyByteBuf buf) {
        return new PacketForgerCrystal(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readItem());
    }

    public static void handle(PacketForgerCrystal msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft instance = Minecraft.getInstance();
            Player player = EnigmaticLegacy.PROXY.getClientPlayer();
            instance.particleEngine.createTrackingEmitter(player, EnigmaticAddonParticles.ICHOR, 40);
            instance.level.playLocalSound(msg.x, msg.y, msg.z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F, false);
            instance.gameRenderer.displayItemActivation(msg.totem);
        });
        context.get().setPacketHandled(true);
    }
}
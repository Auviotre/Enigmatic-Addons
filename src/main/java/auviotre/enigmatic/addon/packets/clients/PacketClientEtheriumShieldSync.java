package auviotre.enigmatic.addon.packets.clients;

import auviotre.enigmatic.addon.contents.objects.etheriumSheild.EtheriumShieldCapability;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.packets.server.PacketEtheriumShieldSync;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class PacketClientEtheriumShieldSync {
    public static void handlePacket(PacketEtheriumShieldSync msg, Supplier<NetworkEvent.Context> contextSupplier) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            SuperAddonHandler.getCapability(player, EtheriumShieldCapability.ETHERIUM_SHIELD_DATA).ifPresent((cap) -> {
                cap.setTick(msg.tick);
            });
        }
    }
}

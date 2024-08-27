package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.packets.PacketCursedXPScrollKey;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.packets.server.PacketXPScrollKey;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class AddonKeybindHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    @OnlyIn(Dist.CLIENT)
    public void onKeyInput(TickEvent.@NotNull ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().isWindowActive() && Minecraft.getInstance().player != null) {
            if (EnigmaticLegacy.keybindHandler.xpScrollKey.consumeClick()) {
                EnigmaticLegacy.packetInstance.send(PacketDistributor.SERVER.noArg(), new PacketXPScrollKey(true));
                EnigmaticAddons.packetInstance.send(PacketDistributor.SERVER.noArg(), new PacketCursedXPScrollKey(true));
            }
        }
    }
}

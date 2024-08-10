package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.packets.PacketCursedXPScrollKey;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;
import com.aizistral.enigmaticlegacy.packets.server.PacketXPScrollKey;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EnigmaticKeybindHandler.class)
public class MixinEnigmaticKeybindHandler {

    @Shadow(remap = false)
    public KeyMapping xpScrollKey;

    @Inject(method = "onKeyInput", at = @At(value = "HEAD"), remap = false)
    private void onKeyInputMix(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        if (this.xpScrollKey.consumeClick()) {
            EnigmaticLegacy.packetInstance.send(PacketDistributor.SERVER.noArg(), new PacketXPScrollKey(true));
            EnigmaticAddons.packetInstance.send(PacketDistributor.SERVER.noArg(), new PacketCursedXPScrollKey(true));
        }
    }
}

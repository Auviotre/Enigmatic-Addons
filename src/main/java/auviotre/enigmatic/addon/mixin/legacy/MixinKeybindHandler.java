package auviotre.enigmatic.addon.mixin.legacy;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(EnigmaticKeybindHandler.class)
public abstract class MixinKeybindHandler {
    @Shadow(remap = false)
    public KeyMapping xpScrollKey;

//    @OnlyIn(Dist.CLIENT)
//    @Inject(method = "onKeyInput", at = @At("HEAD"), remap = false)
//    private void onKeyInput(TickEvent.ClientTickEvent event, CallbackInfo ci) {
//        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().isWindowActive() && Minecraft.getInstance().player != null) {
//            if (this.xpScrollKey.consumeClick()) {
//                EnigmaticLegacy.packetInstance.send(PacketDistributor.SERVER.noArg(), new PacketXPScrollKey(true));
//                EnigmaticAddons.packetInstance.send(PacketDistributor.SERVER.noArg(), new PacketCursedXPScrollKey(true));
//            }
//        }
//    }
}

package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.contents.items.EtheriumCore;
import com.aizistral.etherium.items.EtheriumArmor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(EtheriumArmor.class)
public class MixinEtheriumArmor {
    @Inject(method = "hasShield", at = @At("RETURN"), cancellable = true, remap = false)
    private static void hasShieldMix(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            cir.setReturnValue(EtheriumCore.hasShield(player));
        }
    }
}

package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.contents.items.BlessRing;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.TwistedMirror;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(TwistedMirror.class)
public class MixinTwistedMirror extends ItemBase {
    @Inject(method = "use", at = @At("RETURN"), cancellable = true)
    public void canEatMix(Level world, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (BlessRing.Helper.betrayalAvailable(player)) {
            if (EnigmaticLegacy.PROXY.isInVanillaDimension(player) && !player.getCooldowns().isOnCooldown(this)) {
                player.startUsingItem(hand);
                if (player instanceof ServerPlayer serverPlayer) {
                    SuperpositionHandler.backToSpawn(serverPlayer);
                    BlessRing.Helper.addBetrayal(player, 20);
                    player.getCooldowns().addCooldown(this, 200);
                }
                cir.setReturnValue(InteractionResultHolder.success(player.getItemInHand(hand)));
            }
        }
    }
}

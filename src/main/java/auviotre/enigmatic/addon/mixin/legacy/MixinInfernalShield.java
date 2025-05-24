package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.contents.items.BlessRing;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.items.InfernalShield;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(InfernalShield.class)
public class MixinInfernalShield extends ItemBase {
    @Inject(method = "use", at = @At("RETURN"), cancellable = true)
    public void useMix(Level world, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (SuperAddonHandler.isTheBlessedOne(player)) {
            player.startUsingItem(hand);
            BlessRing.Helper.addBetrayal(player, 1);
            cir.setReturnValue(InteractionResultHolder.consume(player.getItemInHand(hand)));
        }
    }

    @Inject(method = "inventoryTick", at = @At("RETURN"))
    public void inventoryTick(ItemStack stack, Level world, Entity holder, int slot, boolean hand, CallbackInfo ci) {
        if (holder instanceof ServerPlayer player) {
            if (player.isOnFire() && SuperAddonHandler.isTheBlessedOne(player) && (player.getMainHandItem() == stack || player.getOffhandItem() == stack) && !player.isInLava()) {
                player.clearFire();
            }
        }
    }
}

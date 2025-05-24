package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.contents.items.BlessRing;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.AstralFruit;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseFood;
import net.minecraft.server.level.ServerPlayer;
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
@Mixin(AstralFruit.class)
public class MixinAstralFruit extends ItemBaseFood {
    @Inject(method = "canEat", at = @At("RETURN"), cancellable = true, remap = false)
    public void canEatMix(Level world, Player player, ItemStack food, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || SuperAddonHandler.isTheBlessedOne(player));
    }

    @Inject(method = "onConsumed", at = @At("HEAD"), remap = false)
    public void onConsumedMix(Level worldIn, Player player, ItemStack food, CallbackInfo ci) {
        if (player instanceof ServerPlayer playerMP && BlessRing.Helper.betrayalAvailable(playerMP)) {
            if (!SuperpositionHandler.getPersistentBoolean(playerMP, "ConsumedAstralFruit", false))
                BlessRing.Helper.addBetrayal(playerMP, 160);
            else BlessRing.Helper.addBetrayal(playerMP, 40);
        }
    }
}

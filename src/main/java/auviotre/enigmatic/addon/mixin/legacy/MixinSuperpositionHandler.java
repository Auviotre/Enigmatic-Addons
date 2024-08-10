package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(SuperpositionHandler.class)
public class MixinSuperpositionHandler {

    @Inject(method = "getCurseAmount(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true, remap = false)
    private static void getCurseAmountMix(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() == EnigmaticAddonItems.HELL_BLADE_CHARM) {
            cir.setReturnValue(cir.getReturnValue() + 2);
        }
    }
}

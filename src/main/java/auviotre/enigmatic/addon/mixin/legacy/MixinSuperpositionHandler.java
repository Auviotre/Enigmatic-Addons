package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(SuperpositionHandler.class)
public abstract class MixinSuperpositionHandler {

    @Inject(method = "getCurseAmount(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true, remap = false)
    private static void getCurseAmountMix(@NotNull ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() == EnigmaticAddonItems.HELL_BLADE_CHARM) {
            cir.setReturnValue(cir.getReturnValue() + 2);
        }
    }

    @Inject(method = "hasItem", at = @At("RETURN"), cancellable = true, remap = false)
    private static void hasItemMix(Player player, Item item, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && player != null && item != EnigmaticAddonItems.ANTIQUE_BAG) {
            cir.setReturnValue(!SuperAddonHandler.findBookInBag(player, item).isEmpty());
        }
    }

//    @Inject(method = "isTheCursedOne", at = @At("RETURN"), cancellable = true, remap = false)
//    private static void isTheCursedOneMix(Player player, CallbackInfoReturnable<Boolean> cir) {
//        if (!cir.getReturnValue()) cir.setReturnValue(hasCurio(player, EnigmaticAddonItems.BLESS_RING));
//    }
}

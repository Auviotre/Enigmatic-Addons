package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {
    @Inject(method = "getSweepingDamageRatio", at = @At("RETURN"), cancellable = true)
    private static void getSweeping(LivingEntity entity, CallbackInfoReturnable<Float> cir) {
        if (SuperpositionHandler.hasCurio(entity, EnigmaticAddonItems.THUNDER_SCROLL)) {
            cir.setReturnValue(Math.max(1.0F, cir.getReturnValue()));
        }
    }

    @Inject(method = "getSneakingSpeedBonus", at = @At("RETURN"), cancellable = true)
    private static void getSneaking(LivingEntity entity, CallbackInfoReturnable<Float> cir) {
        if (SuperpositionHandler.hasCurio(entity, EnigmaticAddonItems.EXPLORER_SCROLL)) {
            cir.setReturnValue(cir.getReturnValue() + 0.12F);
        }
    }
}

package auviotre.enigmatic.addon.mixin.legacy;

import com.aizistral.enigmaticlegacy.api.items.IAdvancedPotionItem;
import com.aizistral.enigmaticlegacy.items.UltimatePotionLingering;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(UltimatePotionLingering.class)
public abstract class MixinUltimatePotionLingering implements IAdvancedPotionItem {
    @Shadow(remap = false)
    public PotionType potionType;

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "isFoil", at = @At("RETURN"), cancellable = true)
    public void isFoilMix(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.potionType == PotionType.ULTIMATE);
    }
}

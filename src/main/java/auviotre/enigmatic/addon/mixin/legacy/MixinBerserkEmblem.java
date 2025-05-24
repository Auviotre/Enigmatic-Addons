package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.contents.items.BlessRing;
import com.aizistral.enigmaticlegacy.items.BerserkEmblem;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotContext;

@Pseudo
@Mixin(BerserkEmblem.class)
public class MixinBerserkEmblem extends ItemBaseCurio {

    @Inject(method = "canEquip", at = @At("RETURN"), cancellable = true, remap = false)
    public void canEquipMix(SlotContext context, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            if (super.canEquip(context, stack)) {
                LivingEntity entity = context.entity();
                if (entity instanceof Player player) {
                    if (BlessRing.Helper.betrayalAvailable(player)) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}

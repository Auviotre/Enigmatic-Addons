package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.contents.items.BlessRing;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.EnchanterPearl;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;

@Pseudo
@Mixin(EnchanterPearl.class)
public class MixinEnchanterPearl extends ItemBaseCurio {

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

    @Inject(method = "isPresent", at = @At("RETURN"), cancellable = true, remap = false)
    public void isPresentMix(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            cir.setReturnValue(SuperAddonHandler.isTheBlessedOne(player) && SuperpositionHandler.hasCurio(player, this));
        }
    }

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true, remap = false)
    public void getAttributeModifiersMix(SlotContext slotContext, UUID uuid, ItemStack stack, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
        if (slotContext.entity() instanceof Player player) {
            Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
            if (SuperAddonHandler.isTheBlessedOne(player)) {
                CuriosApi.addSlotModifier(attributes, "charm", UUID.fromString("be132e6a-031d-4e61-b15e-652d7051131f"), 1.0, AttributeModifier.Operation.ADDITION);
                cir.setReturnValue(attributes);
            }
        }
    }
}

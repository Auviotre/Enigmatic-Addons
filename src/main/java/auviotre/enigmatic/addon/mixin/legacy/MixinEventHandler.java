package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.api.items.IBlessed;
import auviotre.enigmatic.addon.contents.items.BlessRing;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EnigmaticEventHandler.class)
public class MixinEventHandler {

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "onTooltip", at = @At("HEAD"), remap = false, cancellable = true)
    public void onTooltipMix(ItemTooltipEvent event, CallbackInfo ci) {
        if (event.getEntity() != null && SuperAddonHandler.isTheBlessedOne(event.getEntity())) {
            ItemStack blessRing = SuperpositionHandler.getCurioStack(event.getEntity(), EnigmaticAddonItems.BLESS_RING);
            int level = BlessRing.Helper.getBlessAttribute(blessRing, BlessRing.CURSE_TIME_LEVEL);
            if (level < 4) return;
            ItemStack item = event.getItemStack();
            if (item.getItem() instanceof IBlessed || BlessRing.Helper.isBetrayedItem(item)) {
                event.getToolTip().replaceAll(component -> {
                    if (component.getContents() instanceof TranslatableContents content) {
                        if (item.getItem() instanceof IBlessed && level > 3) {
                            if (content.getKey().equals("tooltip.enigmaticlegacy.cursedOnesOnly1"))
                                return Component.translatable("tooltip.enigmaticaddons.blessTrueUse1").withStyle(ChatFormatting.GOLD);
                            if (content.getKey().equals("tooltip.enigmaticlegacy.cursedOnesOnly2"))
                                return Component.translatable("tooltip.enigmaticaddons.blessTrueUse2").withStyle(ChatFormatting.GOLD);
                        } else if (level > 4) {
                            if (content.getKey().equals("tooltip.enigmaticlegacy.cursedOnesOnly1"))
                                return Component.translatable("tooltip.enigmaticaddons.blessUse1").withStyle(ChatFormatting.RED);
                            if (content.getKey().equals("tooltip.enigmaticlegacy.cursedOnesOnly2"))
                                return Component.translatable("tooltip.enigmaticaddons.blessUse2").withStyle(ChatFormatting.RED);
                        }
                    }
                    return component;
                });
                ci.cancel();
            }
        }
    }
}

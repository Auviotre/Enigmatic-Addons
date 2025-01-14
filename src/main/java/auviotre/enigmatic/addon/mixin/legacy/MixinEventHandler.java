package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.contents.items.BlessRing;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.registries.ForgeRegistries;
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
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
            if (BlessRing.blessList.contains(key.getNamespace() + ":" + key.getPath())) {
                event.getToolTip().replaceAll(component -> {
                    if (component.getContents() instanceof TranslatableContents content) {
                        if (content.getKey().equals("tooltip.enigmaticlegacy.cursedOnesOnly1"))
                            return Component.translatable("tooltip.enigmaticaddons.blessUse1").withStyle(ChatFormatting.YELLOW);
                        if (content.getKey().equals("tooltip.enigmaticlegacy.cursedOnesOnly2"))
                            return Component.translatable("tooltip.enigmaticaddons.blessUse2").withStyle(ChatFormatting.YELLOW);
                    }
                    return component;
                });
                ci.cancel();
            }
        }
    }
}

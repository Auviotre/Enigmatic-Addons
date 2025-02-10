package auviotre.enigmatic.addon.client.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.gui.GUIUtils;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEffects;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.caelus.api.RenderCapeEvent;

@Mod.EventBusSubscriber(modid = EnigmaticAddons.MODID)
public class ClientEventHandler {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onTooltipRendering(RenderTooltipEvent.@NotNull Color event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof ItemBase item && !ForgeRegistries.ITEMS.getKey(item).getNamespace().equals(EnigmaticLegacy.MODID)) {
                int background = GUIUtils.DEFAULT_BACKGROUND_COLOR;
                int borderStart = GUIUtils.DEFAULT_BORDER_COLOR_START;
                int borderEnd = GUIUtils.DEFAULT_BORDER_COLOR_END;

                if (item instanceof ICursed || item instanceof CursedRing) {
                    background = 0xF7101010;
                    borderStart = 0x50FF0C00;
                    borderEnd = borderStart;
                } else if (item == EnigmaticItems.COSMIC_SCROLL) {
                    background = 0xF0100010;
                    borderStart = 0xB0A800A8;
                    borderEnd = (borderStart & 0x3E3E3E) >> 1 | borderStart & 0xFF000000;
                }
                event.setBackground(background);
                event.setBorderStart(borderStart);
                event.setBorderEnd(borderEnd);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderFireOverlay(RenderBlockScreenEffectEvent event) {
        Player player = event.getPlayer();
        if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE) {
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.SCORCHED_CHARM)) {
                event.setCanceled(true);
                return;
            }
            if (SuperpositionHandler.hasCurio(player, EnigmaticItems.BLAZING_CORE) || player.hasEffect(EnigmaticEffects.MOLTEN_HEART))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void renderCape(RenderCapeEvent event) {
        if (SuperAddonHandler.getChaosElytra(event.getEntity()) != null) {
            event.setCanceled(true);
        }
    }
}

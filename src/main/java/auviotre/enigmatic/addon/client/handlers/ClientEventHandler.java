package auviotre.enigmatic.addon.client.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.api.items.IBlessed;
import auviotre.enigmatic.addon.handlers.OmniconfigAddonHandler;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.gui.GUIUtils;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEffects;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.etherium.items.EtheriumArmor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.caelus.api.RenderCapeEvent;

@Mod.EventBusSubscriber(modid = EnigmaticAddons.MODID)
public class ClientEventHandler {
    public static final ResourceLocation MC_ICONS = new ResourceLocation("textures/gui/icons.png");
    public static final ResourceLocation ICONS_LOCATION = new ResourceLocation(EnigmaticAddons.MODID, "textures/gui/icons.png");

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onTooltipRendering(RenderTooltipEvent.@NotNull Color event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            if (!ForgeRegistries.ITEMS.getKey(item).getNamespace().equals(EnigmaticLegacy.MODID)) {
                int background = GUIUtils.DEFAULT_BACKGROUND_COLOR;
                int borderStart = GUIUtils.DEFAULT_BORDER_COLOR_START;
                int borderEnd = GUIUtils.DEFAULT_BORDER_COLOR_END;

                if (item instanceof ICursed || item instanceof CursedRing) {
                    if (item instanceof IBlessed && Minecraft.getInstance().player != null && SuperAddonHandler.isTheBlessedOne(Minecraft.getInstance().player))
                        borderStart = 0x80FFA632;
                    else borderStart = 0x50FF0C00;
                    background = 0xF7101010;
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(receiveCanceled = true)
    public void onOverlayRender(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (event.getOverlay().equals(VanillaGuiOverlay.PLAYER_HEALTH.type()) && OmniconfigAddonHandler.etheriumShieldIcon.getValue() && EtheriumArmor.hasShield(player)) {
            if (player.isCreative() || player.isSpectator()) return;
            GuiGraphics graphics = event.getGuiGraphics();
            PoseStack pose = graphics.pose();
            int x = event.getWindow().getGuiScaledWidth() / 2 - 92;

            int xCorrection = 0;
            int yCorrection = 0;
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, ICONS_LOCATION);
            int l = event.getWindow().getGuiScaledHeight() - 32 - 8;
            graphics.blit(ICONS_LOCATION, x + xCorrection, l + yCorrection, 0, 16, 83, 11, 256, 256);
            RenderSystem.disableBlend();
            RenderSystem.setShaderTexture(0, MC_ICONS);
        }
    }
}

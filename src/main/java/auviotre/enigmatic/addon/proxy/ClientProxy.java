package auviotre.enigmatic.addon.proxy;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.client.renderers.DragonBreathArrowRenderer;
import auviotre.enigmatic.addon.client.renderers.EmptyRenderer;
import auviotre.enigmatic.addon.client.renderers.UltimateDragonFireballRenderer;
import auviotre.enigmatic.addon.client.screens.AntiqueBagScreen;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.registries.EnigmaticAddonMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ClientProxy extends CommonProxy {
    public ClientProxy() {
    }

    public void clientInit() {
        initEntityRenderers();
        initItemProperties();
        MenuScreens.register(EnigmaticAddonMenus.ANTIQUE_BAG_MENU, AntiqueBagScreen::new);
    }

    public void initItemProperties() {
        try {
            ItemProperties.register(EnigmaticAddonItems.EARTH_PROMISE, new ResourceLocation("broken"), (stack, world, living, j) -> living instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()) ? 1.0F : 0.0F);
            ItemProperties.register(EnigmaticAddonItems.DRAGON_BOW, new ResourceLocation("pulling"), (stack, world, living, j) -> living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(EnigmaticAddonItems.DRAGON_BOW, new ResourceLocation("pull"), (stack, level, living, i) -> living == null ? 0.0F : living.getUseItem() != stack ? 0.0F : (float) (stack.getUseDuration() - living.getUseItemRemainingTicks()) / 20.0F);
        } catch (Exception exception) {
            EnigmaticAddons.LOGGER.warn("Could not load item models.");
        }
    }

    private void initEntityRenderers() {
        EntityRenderers.register(EnigmaticAddonEntities.DRAGON_BREATH_ARROW, DragonBreathArrowRenderer::new);
        EntityRenderers.register(EnigmaticAddonEntities.COBWEB_BALL, EmptyRenderer::new);
        EntityRenderers.register(EnigmaticAddonEntities.SPLIT_DRAGON_BREATH, EmptyRenderer::new);
        EntityRenderers.register(EnigmaticAddonEntities.ULTIMATE_DRAGON_FIREBALL, UltimateDragonFireballRenderer::new);
    }
}

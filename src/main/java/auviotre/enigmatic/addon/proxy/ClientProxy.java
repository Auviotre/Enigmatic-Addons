package auviotre.enigmatic.addon.proxy;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.client.renderers.*;
import auviotre.enigmatic.addon.client.renderers.layers.ChaosElytraLayer;
import auviotre.enigmatic.addon.client.screens.AntiqueBagScreen;
import auviotre.enigmatic.addon.client.screens.ArtificialFlowerScreen;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.registries.EnigmaticAddonMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientProxy extends CommonProxy {
    public ClientProxy() {
    }

    public void clientInit() {
        initEntityRenderers();
        initItemProperties();
        MenuScreens.register(EnigmaticAddonMenus.ANTIQUE_BAG_MENU, AntiqueBagScreen::new);
        MenuScreens.register(EnigmaticAddonMenus.ARTIFICIAL_FLOWER_MENU, ArtificialFlowerScreen::new);
    }

    public void initItemProperties() {
        try {
            ItemProperties.register(EnigmaticAddonItems.DISASTER_SWORD, new ResourceLocation("blocking"), (stack, world, living, j) -> living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(EnigmaticAddonItems.EARTH_PROMISE, new ResourceLocation("broken"), (stack, world, living, j) -> living instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()) ? 1.0F : 0.0F);
            ItemProperties.register(EnigmaticAddonItems.ASTRAL_SPEAR, new ResourceLocation("using"), (stack, world, living, j) -> living != null && living.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(EnigmaticAddonItems.ICHOR_SPEAR, new ResourceLocation("using"), (stack, world, living, j) -> living != null && living.getUseItem() == stack ? 1.0F : 0.0F);
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
        EntityRenderers.register(EnigmaticAddonEntities.ICHOR_SPEAR, ThrownIchorSpearRenderer::new);
        EntityRenderers.register(EnigmaticAddonEntities.ASTRAL_SPEAR, ThrownAstralSpearRenderer::new);
        EntityRenderers.register(EnigmaticAddonEntities.EVIL_DAGGER, ThrownEvilDaggerRenderer::new);
        EntityRenderers.register(EnigmaticAddonEntities.DISASTER_CHAOS, EmptyRenderer::new);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void addLayers(EntityRenderersEvent.AddLayers event) {
        this.addPlayerLayer(event, "default");
        this.addPlayerLayer(event, "slim");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @OnlyIn(Dist.CLIENT)
    private <T extends LivingEntity, M extends EntityModel<T>> void addPlayerLayer(EntityRenderersEvent.AddLayers event, String skin) {
        EntityRenderer<? extends Player> renderer = event.getSkin(skin);
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new ChaosElytraLayer(livingRenderer, event.getEntityModels()));
        }
    }
}

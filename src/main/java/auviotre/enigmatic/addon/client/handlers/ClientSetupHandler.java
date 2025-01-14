package auviotre.enigmatic.addon.client.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.client.particles.ChaosParticle;
import auviotre.enigmatic.addon.client.particles.IchorParticle;
import auviotre.enigmatic.addon.client.particles.StarDustParticle;
import auviotre.enigmatic.addon.helpers.PotionAddonHelper;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = EnigmaticAddons.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientSetupHandler {

    @SubscribeEvent
    public static void onColorInit(RegisterColorHandlersEvent.Item event) {
        EnigmaticAddons.LOGGER.info("Initializing colors registration...");
        event.register((stack, color) -> {
            if (PotionHelper.isAdvancedPotion(stack)) return color > 0 ? -1 : PotionAddonHelper.getColor(stack);
            else return color > 0 ? -1 : PotionUtils.getColor(stack);
        }, EnigmaticAddonItems.ULTIMATE_POTION, EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonItems.ULTIMATE_POTION_SPLASH, EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonItems.ULTIMATE_POTION_LINGERING, EnigmaticAddonItems.COMMON_POTION_LINGERING);
        EnigmaticAddons.LOGGER.info("Colors registered successfully.");
    }

    @SubscribeEvent
    public static void onRegisterParticles(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(EnigmaticAddonParticles.BLUE_STAR_DUST, StarDustParticle.BlueProvider::new);
        event.registerSpriteSet(EnigmaticAddonParticles.RED_STAR_DUST, StarDustParticle.RedProvider::new);
        event.registerSpriteSet(EnigmaticAddonParticles.PURPLE_STAR_DUST, StarDustParticle.PurpleProvider::new);
        event.registerSpriteSet(EnigmaticAddonParticles.ICHOR, IchorParticle.Provider::new);
        event.registerSpriteSet(EnigmaticAddonParticles.ABYSS_CHAOS, ChaosParticle.Provider::new);
    }
}

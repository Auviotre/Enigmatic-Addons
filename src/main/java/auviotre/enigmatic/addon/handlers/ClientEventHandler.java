package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.client.particles.StarDustParticle;
import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(
        modid = EnigmaticAddons.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onRegisterParticles(final @NotNull RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(EnigmaticAddonParticles.BLUE_STAR_DUST, StarDustParticle.BlueProvider::new);
        event.registerSpriteSet(EnigmaticAddonParticles.RED_STAR_DUST, StarDustParticle.RedProvider::new);
        event.registerSpriteSet(EnigmaticAddonParticles.PURPLE_STAR_DUST, StarDustParticle.PurpleProvider::new);
    }
}

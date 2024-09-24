package auviotre.enigmatic.addon.client.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.helpers.PotionAddonHelper;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(
        modid = EnigmaticAddons.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = {Dist.CLIENT}
)
public class ClientSetupHandler {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onColorInit(RegisterColorHandlersEvent.@NotNull Item event) {
        EnigmaticAddons.LOGGER.info("Initializing colors registration...");
        event.register((stack, color) -> {
            if (PotionHelper.isAdvancedPotion(stack)) return color > 0 ? -1 : PotionAddonHelper.getColor(stack);
            else return color > 0 ? -1 : PotionUtils.getColor(stack);
        }, EnigmaticAddonItems.ULTIMATE_POTION, EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonItems.ULTIMATE_POTION_SPLASH, EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonItems.ULTIMATE_POTION_LINGERING, EnigmaticAddonItems.COMMON_POTION_LINGERING);
        EnigmaticAddons.LOGGER.info("Colors registered successfully.");
    }
}

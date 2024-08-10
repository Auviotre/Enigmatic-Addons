package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.helpers.JEIBrewingHelper;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.List;

@JeiPlugin
public class AddonJEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation(EnigmaticAddons.MODID, "common_plugin");

    public ResourceLocation getPluginUid() {
        return ID;
    }

    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(EnigmaticItems.COMMON_POTION, JEIBrewingHelper.SubtypeInterpreter.LEGACY_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticItems.COMMON_POTION_SPLASH, JEIBrewingHelper.SubtypeInterpreter.LEGACY_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticItems.COMMON_POTION_LINGERING, JEIBrewingHelper.SubtypeInterpreter.LEGACY_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticItems.ULTIMATE_POTION, JEIBrewingHelper.SubtypeInterpreter.LEGACY_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticItems.ULTIMATE_POTION_SPLASH, JEIBrewingHelper.SubtypeInterpreter.LEGACY_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticItems.ULTIMATE_POTION_LINGERING, JEIBrewingHelper.SubtypeInterpreter.LEGACY_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticAddonItems.COMMON_POTION, JEIBrewingHelper.SubtypeInterpreter.ADDON_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticAddonItems.COMMON_POTION_SPLASH, JEIBrewingHelper.SubtypeInterpreter.ADDON_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticAddonItems.COMMON_POTION_LINGERING, JEIBrewingHelper.SubtypeInterpreter.ADDON_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticAddonItems.ULTIMATE_POTION, JEIBrewingHelper.SubtypeInterpreter.ADDON_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticAddonItems.ULTIMATE_POTION_SPLASH, JEIBrewingHelper.SubtypeInterpreter.ADDON_INSTANCE);
        registration.registerSubtypeInterpreter(EnigmaticAddonItems.ULTIMATE_POTION_LINGERING, JEIBrewingHelper.SubtypeInterpreter.ADDON_INSTANCE);
    }

    public void registerRecipes(IRecipeRegistration registration) {
        IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();
        List<IJeiBrewingRecipe> brewingRecipes = JEIBrewingHelper.getBrewingRecipes(vanillaRecipeFactory);
        brewingRecipes.sort(Comparator.comparingInt(IJeiBrewingRecipe::getBrewingSteps));
        registration.addRecipes(RecipeTypes.BREWING, brewingRecipes);
    }
}

package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.contents.crafting.DragonBowBrewingRecipe;
import auviotre.enigmatic.addon.contents.crafting.EnabledCondition;
import auviotre.enigmatic.addon.contents.crafting.EnchantmentAmplifierRecipe;
import auviotre.enigmatic.addon.contents.crafting.PrimevalCubeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegisterEvent;

public class EnigmaticAddonRecipes extends AbstractRegistry<RecipeSerializer<?>> {
    private static final EnigmaticAddonRecipes INSTANCE = new EnigmaticAddonRecipes();
    @ObjectHolder(value = "enigmaticaddons:primeval_cube", registryName = "recipe_serializer")
    public static final RecipeSerializer<PrimevalCubeRecipe> PRIMEVAL_CUBE_RECIPE = null;

    @ObjectHolder(value = "enigmaticaddons:enchantment_amplifying", registryName = "recipe_serializer")
    public static final RecipeSerializer<EnchantmentAmplifierRecipe> ENCHANTMENT_AMPLIFIER_RECIPE = null;

    @ObjectHolder(value = "enigmaticaddons:dragon_bow_brewing", registryName = "recipe_serializer")
    public static final RecipeSerializer<DragonBowBrewingRecipe> DRAGON_BOW_BREWING_RECIPE = null;

    private EnigmaticAddonRecipes() {
        super(ForgeRegistries.RECIPE_SERIALIZERS);
        this.register("primeval_cube", () -> PrimevalCubeRecipe.SERIALIZER);
        this.register("enchantment_amplifying", () -> EnchantmentAmplifierRecipe.SERIALIZER);
        this.register("dragon_bow_brewing", () -> DragonBowBrewingRecipe.SERIALIZER);
    }

    protected void onRegister(RegisterEvent event) {
        CraftingHelper.register(EnabledCondition.Serializer.INSTANCE);
    }
}

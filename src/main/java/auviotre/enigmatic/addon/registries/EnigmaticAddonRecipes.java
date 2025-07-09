package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.contents.crafting.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegisterEvent;

public class EnigmaticAddonRecipes extends AbstractRegistry<RecipeSerializer<?>> {
    @ObjectHolder(value = "enigmaticaddons:primeval_cube", registryName = "recipe_serializer")
    public static final RecipeSerializer<PrimevalCubeRecipe> PRIMEVAL_CUBE_RECIPE = null;
    @ObjectHolder(value = "enigmaticaddons:enchantment_amplifying", registryName = "recipe_serializer")
    public static final RecipeSerializer<EnchantmentAmplifierRecipe> ENCHANTMENT_AMPLIFIER_RECIPE = null;
    @ObjectHolder(value = "enigmaticaddons:dragon_bow_brewing", registryName = "recipe_serializer")
    public static final RecipeSerializer<DragonBowBrewingRecipe> DRAGON_BOW_BREWING_RECIPE = null;
    @ObjectHolder(value = "enigmaticaddons:violence_scroll_absorbing", registryName = "recipe_serializer")
    public static final RecipeSerializer<ViolenceScrollAbsorbingRecipe> VIOLENCE_SCROLL_ABSORB_RECIPE = null;
    private static final EnigmaticAddonRecipes INSTANCE = new EnigmaticAddonRecipes();

    private EnigmaticAddonRecipes() {
        super(ForgeRegistries.RECIPE_SERIALIZERS);
        this.register("primeval_cube", () -> PrimevalCubeRecipe.SERIALIZER);
        this.register("enchantment_amplifying", () -> EnchantmentAmplifierRecipe.SERIALIZER);
        this.register("dragon_bow_brewing", () -> DragonBowBrewingRecipe.SERIALIZER);
        this.register("violence_scroll_absorbing", () -> ViolenceScrollAbsorbingRecipe.SERIALIZER);
    }

    protected void onRegister(RegisterEvent event) {
        CraftingHelper.register(EnabledCondition.Serializer.INSTANCE);
    }
}

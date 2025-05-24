package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.items.TotemOfMalice;
import auviotre.enigmatic.addon.helpers.JEIBrewingHelper;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.crafting.HiddenRecipe;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class AddonJEIPlugin implements IModPlugin {
    public static final RecipeType<CraftingRecipe> HIDDEN_CRAFTING = RecipeType.create(EnigmaticLegacy.MODID, "hidden_crafting", CraftingRecipe.class);
    private static final ResourceLocation ID = new ResourceLocation(EnigmaticAddons.MODID, "common_plugin");

    public ResourceLocation getPluginUid() {
        return ID;
    }

    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        registration.addRecipeCategories(new HiddenRecipeCategory(guiHelper));
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
        // TOTEM_OF_MALICE
        ItemStack stack = EnigmaticAddonItems.TOTEM_OF_MALICE.getDefaultInstance();
        TotemOfMalice.setTotemPower(stack, 3);
        IJeiAnvilRecipe totemRecipe = vanillaRecipeFactory.createAnvilRecipe(
                EnigmaticAddonItems.TOTEM_OF_MALICE.getDefaultInstance(),
                List.of(EnigmaticItems.EVIL_ESSENCE.getDefaultInstance()),
                List.of(stack),
                new ResourceLocation(EnigmaticAddons.MODID, "material_repair.totem_of_malice")
        );
        // EXTRADIMENSIONAL_SCEPTER
        stack = EnigmaticAddonItems.EXTRADIMENSIONAL_SCEPTER.getDefaultInstance();
        stack.setDamageValue(stack.getMaxDamage());
        ItemStack output = EnigmaticAddonItems.EXTRADIMENSIONAL_SCEPTER.getDefaultInstance();
        output.setDamageValue(Mth.ceil(output.getMaxDamage() * 0.75));
        IJeiAnvilRecipe scepterRecipe = vanillaRecipeFactory.createAnvilRecipe(
                stack,
                List.of(EnigmaticItems.EXTRADIMENSIONAL_EYE.getDefaultInstance()),
                List.of(output),
                new ResourceLocation(EnigmaticAddons.MODID, "material_repair.extradimensional_scepter")
        );
        registration.addRecipes(RecipeTypes.ANVIL, List.of(totemRecipe, scepterRecipe));
        if (OmniconfigAddonHandler.HiddenRecipeJEIDisplay.getValue()) {
            registration.addRecipes(HIDDEN_CRAFTING, getHiddenRecipe());
        }
    }

    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Blocks.CRAFTING_TABLE), HIDDEN_CRAFTING);
    }

    private List<CraftingRecipe> getHiddenRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        ForgeRegistries.ITEMS.forEach((item) -> {
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
            Map.Entry<ItemStack[][], ItemStack> recipe = HiddenRecipe.getRecipe(itemKey);
            if (!recipe.getValue().isEmpty()) {
                ItemStack[][] inputs = recipe.getKey();
                NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        ingredients.set(3 * i + j, Ingredient.of(inputs[i][j]));
                    }
                }
                ShapedRecipe shapedRecipe = new ShapedRecipe(itemKey, "", CraftingBookCategory.MISC, 3, 3, ingredients, item.getDefaultInstance());
                recipes.add(shapedRecipe);
            }
        });
        return recipes;
    }

    public static class HiddenRecipeCategory extends AbstractRecipeCategory<CraftingRecipe> {
        private final IDrawable background;

        public HiddenRecipeCategory(IGuiHelper guiHelper) {
            super(HIDDEN_CRAFTING, Component.translatable("gui.jei.enigmaticlegacy.category.hidden_crafting"), guiHelper.createDrawableItemStack(new ItemStack(Blocks.CRAFTING_TABLE)), 116, 54);
            ResourceLocation location = new ResourceLocation(EnigmaticAddons.MODID, "textures/gui/jei_hidden_crafting.png");
            this.background = guiHelper.createDrawable(location, 0, 0, 116, 54);
        }

        public void draw(CraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
            this.background.draw(guiGraphics);
        }

        public void setRecipe(IRecipeLayoutBuilder builder, CraftingRecipe recipe, IFocusGroup focuses) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (int y = 0; y < 3; ++y)
                for (int x = 0; x < 3; ++x)
                    builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1).addIngredients(ingredients.get(y * 3 + x));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19).addItemStack(recipe.getResultItem(null));
        }
    }
}

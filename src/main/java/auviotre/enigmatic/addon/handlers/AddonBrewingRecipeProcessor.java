package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.contents.brewing.AddonBrewingRecipe;
import auviotre.enigmatic.addon.contents.brewing.AstralBrewingRecipe;
import com.aizistral.enigmaticlegacy.brewing.AbstractBrewingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddonBrewingRecipeProcessor implements IComponentProcessor {
    private AbstractBrewingRecipe recipe;

    public static List<IVariable> wrapStackList(ItemStack... stackList) {
        List<IVariable> variableList = new ArrayList<>();
        for (ItemStack catalyst : stackList) {
            variableList.add(IVariable.from(catalyst));
        }
        return variableList;
    }

    public static List<IVariable> wrapIngredientSet(Iterable<Ingredient> ingredientSet) {
        List<IVariable> variableList = new ArrayList<>();
        for (Ingredient ingredient : ingredientSet) {
            variableList.addAll(wrapStackList(ingredient.getItems()));
        }
        return variableList;
    }

    public void setup(Level level, IVariableProvider variables) {
        ResourceLocation recipeId = new ResourceLocation(variables.get("recipe").asString());
        int index = variables.get("index").asNumber().intValue();
        this.recipe = AbstractBrewingRecipe.recipeMap.containsKey(recipeId) ? (AbstractBrewingRecipe) ((List<?>) AbstractBrewingRecipe.recipeMap.get(recipeId)).get(index) : AbstractBrewingRecipe.EMPTY_RECIPE;
    }

    public IVariable process(Level level, String key) {
        if (this.recipe instanceof AstralBrewingRecipe astral) {
            if (key.startsWith("catalyst")) {
                return IVariable.wrapList(wrapStackList(astral.getIngredient().getItems()));
            }

            if (key.startsWith("input")) {
                return IVariable.wrapList(wrapStackList(astral.getInput().getItems()));
            }

            if (key.startsWith("output")) {
                return IVariable.from(astral.getOutput());
            }
        } else if (this.recipe instanceof AddonBrewingRecipe addon) {
            HashMap<Ingredient, Ingredient> processingMappings = addon.getProcessingMappings();
            if (key.startsWith("catalyst")) {
                return IVariable.wrapList(wrapIngredientSet(processingMappings.values()));
            }

            if (key.startsWith("input")) {
                return IVariable.wrapList(wrapIngredientSet(processingMappings.keySet()));
            }

            if (key.startsWith("output")) {
                return IVariable.from(addon.getOutput());
            }
        }

        return null;
    }
}
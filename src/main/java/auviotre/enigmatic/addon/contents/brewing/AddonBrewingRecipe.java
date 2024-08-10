package auviotre.enigmatic.addon.contents.brewing;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.helpers.PotionAddonHelper;
import com.aizistral.enigmaticlegacy.brewing.AbstractBrewingRecipe;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;

public class AddonBrewingRecipe extends AbstractBrewingRecipe {
    @Nonnull
    private final HashMap<Ingredient, Ingredient> processingMappings;
    @Nonnull
    private final ItemStack output;

    public AddonBrewingRecipe(HashMap<Ingredient, Ingredient> ingredientCompliance, ItemStack output) {
        this(ingredientCompliance, output, new ResourceLocation(EnigmaticAddons.MODID, ItemNBTHelper.getString(output, "EnigmaticPotion", "unknown")));
    }

    public AddonBrewingRecipe(HashMap<Ingredient, Ingredient> ingredientCompliance, ItemStack output, ResourceLocation name) {
        super(name);
        this.processingMappings = ingredientCompliance;
        this.output = output;
    }

    public boolean isInput(@Nonnull ItemStack stack) {
        Iterator<Ingredient> var2 = this.processingMappings.keySet().iterator();
        Ingredient ing;
        do {
            if (!var2.hasNext()) return false;
            ing = var2.next();
        } while (!this.isInput(stack, ing));
        return true;
    }

    public boolean isInput(@Nonnull ItemStack stack, @NotNull Ingredient ingredient) {
        ItemStack[] ingredients = ingredient.getItems();
        for (ItemStack testStack : ingredients) {
            if (testStack.getItem().equals(stack.getItem()) && PotionUtils.getPotion(testStack).equals(PotionUtils.getPotion(stack)) && PotionAddonHelper.getAdvancedPotion(testStack).equals(PotionAddonHelper.getAdvancedPotion(stack))) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        Iterator<Ingredient> var3 = this.processingMappings.keySet().iterator();
        Ingredient ing;
        do {
            if (!var3.hasNext()) return ItemStack.EMPTY;
            ing = var3.next();
        } while (!this.isInput(input, ing) || !this.processingMappings.get(ing).test(ingredient));

        return this.output.copy();
    }

    public boolean isIngredient(ItemStack stack) {
        for (Ingredient ing : this.processingMappings.values()) {
            if (ing.test(stack)) {
                return true;
            }
        }
        return false;
    }

    public HashMap<Ingredient, Ingredient> getProcessingMappings() {
        return this.processingMappings;
    }

    public ItemStack getOutput() {
        return this.output;
    }
}

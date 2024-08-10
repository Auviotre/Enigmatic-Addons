package auviotre.enigmatic.addon.contents.brewing;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.brewing.AbstractBrewingRecipe;
import com.aizistral.enigmaticlegacy.registries.EnigmaticBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public class AstralBrewingRecipe extends AbstractBrewingRecipe {
    private final Ingredient input = Ingredient.of(Items.HONEY_BOTTLE);
    private final Ingredient ingredient = Ingredient.of(EnigmaticBlocks.ASTRAL_BLOCK);
    private final ItemStack output = new ItemStack(EnigmaticAddonItems.ASTRAL_POTION);


    public AstralBrewingRecipe(ResourceLocation name) {
        super(name);
    }

    public boolean isInput(@Nonnull ItemStack stack) {
        ItemStack[] items = this.getInput().getItems();
        for (ItemStack testStack : items) {
            if (testStack.getItem() == stack.getItem() && PotionUtils.getPotion(testStack) == PotionUtils.getPotion(stack)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        return this.isInput(input) && this.isIngredient(ingredient) ? this.getOutput().copy() : ItemStack.EMPTY;
    }

    public Ingredient getInput() {
        return this.input;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public boolean isIngredient(ItemStack ingredient) {
        return this.ingredient.test(ingredient);
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }
}

package auviotre.enigmatic.addon.contents.brewing;

import com.aizistral.enigmaticlegacy.brewing.AbstractBrewingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public class AstralBrewingRecipe extends AbstractBrewingRecipe {
    private final Ingredient input = Ingredient.of(Items.HONEY_BOTTLE);
    private final Ingredient ingredient;
    private final ItemStack output;


    public AstralBrewingRecipe(Ingredient ingredient, ItemStack output, ResourceLocation name) {
        super(name);
        this.ingredient = ingredient;
        this.output = output;
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

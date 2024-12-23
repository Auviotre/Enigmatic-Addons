package auviotre.enigmatic.addon.helpers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.brewing.AddonBrewingRecipe;
import auviotre.enigmatic.addon.handlers.OmniconfigAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.items.IAdvancedPotionItem;
import com.aizistral.enigmaticlegacy.brewing.AbstractBrewingRecipe;
import com.aizistral.enigmaticlegacy.brewing.ComplexBrewingRecipe;
import com.aizistral.enigmaticlegacy.brewing.SpecialBrewingRecipe;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;
import com.aizistral.enigmaticlegacy.objects.AdvancedPotion;
import com.aizistral.enigmaticlegacy.registries.EnigmaticBlocks;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JEIBrewingHelper {
    public static List<IJeiBrewingRecipe> getBrewingRecipes(IVanillaRecipeFactory vanillaRecipeFactory) {
        Collection<IBrewingRecipe> brewingRecipes = BrewingRecipeRegistry.getRecipes();
        Objects.requireNonNull(AbstractBrewingRecipe.class);
        Set<IJeiBrewingRecipe> recipes = new HashSet<>();
        if (OmniconfigAddonHandler.isItemEnabled(EnigmaticAddonItems.ASTRAL_POTION)) {
            AstralRecipe astralRecipe = new AstralRecipe(null);
            recipes.add(astralRecipe);
        }
        for (IBrewingRecipe iBrewingRecipe : brewingRecipes) {
            if (iBrewingRecipe instanceof AbstractBrewingRecipe brewingRecipe) {
                if (brewingRecipe instanceof ComplexBrewingRecipe complex) {
                    HashMap<Ingredient, Ingredient> processingMappings = complex.getProcessingMappings();
                    Set<Ingredient> potions = processingMappings.keySet();
                    if (!potions.isEmpty()) {
                        ItemStack output = complex.getOutput();
                        for (Ingredient potion : potions) {
                            ItemStack[] ingredient = processingMappings.get(potion).getItems();
                            List<ItemStack> inputs = Arrays.stream(potion.getItems()).filter((stack) -> !stack.isEmpty()).toList();
                            if (!output.isEmpty() && !inputs.isEmpty()) {
                                ResourceLocation location = new ResourceLocation(EnigmaticLegacy.MODID, ItemNBTHelper.getString(output, "EnigmaticPotion", "unknown"));
                                IJeiBrewingRecipe recipe = new EnigmaticRecipe(List.of(ingredient), inputs, output, location, vanillaRecipeFactory);
                                recipes.add(recipe);
                            }
                        }
                    }
                } else if (brewingRecipe instanceof AddonBrewingRecipe addon) {
                    HashMap<Ingredient, Ingredient> processingMappings = addon.getProcessingMappings();
                    Set<Ingredient> potions = processingMappings.keySet();
                    if (!potions.isEmpty()) {
                        ItemStack output = addon.getOutput();
                        for (Ingredient potion : potions) {
                            ItemStack[] ingredient = processingMappings.get(potion).getItems();
                            List<ItemStack> inputs = Arrays.stream(potion.getItems()).filter((stack) -> !stack.isEmpty()).toList();
                            if (!output.isEmpty() && !inputs.isEmpty()) {
                                ResourceLocation location = new ResourceLocation(EnigmaticAddons.MODID, ItemNBTHelper.getString(output, "EnigmaticPotion", "unknown"));
                                IJeiBrewingRecipe recipe = new EnigmaticRecipe(List.of(ingredient), inputs, output, location, vanillaRecipeFactory);
                                recipes.add(recipe);
                            }
                        }
                    }
                } else if (brewingRecipe instanceof SpecialBrewingRecipe special) {
                    ItemStack[] ingredients = special.getIngredient().getItems();
                    if (ingredients.length > 0) {
                        Ingredient inputIngredient = special.getInput();
                        ItemStack output = special.getOutput();
                        List<ItemStack> inputs = Arrays.stream(inputIngredient.getItems()).filter((stack) -> !stack.isEmpty()).toList();
                        if (!output.isEmpty() && !inputs.isEmpty()) {
                            IJeiBrewingRecipe recipe = vanillaRecipeFactory.createBrewingRecipe(List.of(ingredients), inputs, output, ForgeRegistries.ITEMS.getKey(output.getItem()));
                            recipes.add(recipe);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(recipes);
    }

    public static class SubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
        public static final SubtypeInterpreter LEGACY_INSTANCE = new SubtypeInterpreter(true);
        public static final SubtypeInterpreter ADDON_INSTANCE = new SubtypeInterpreter(false);

        private final boolean legacy;

        private SubtypeInterpreter(boolean legacy) {
            this.legacy = legacy;
        }

        public String apply(@NotNull ItemStack itemStack, UidContext context) {
            if (!itemStack.hasTag() || !(itemStack.getItem() instanceof IAdvancedPotionItem)) {
                return "";
            } else {
                IAdvancedPotionItem.PotionType potionType = ((IAdvancedPotionItem) itemStack.getItem()).getPotionType();
                AdvancedPotion advancedPotion = this.legacy ? PotionHelper.getAdvancedPotion(itemStack) : PotionAddonHelper.getAdvancedPotion(itemStack);
                StringBuilder stringBuilder = new StringBuilder(advancedPotion.getId()).append(";").append(potionType.toString());
                for (MobEffectInstance effect : advancedPotion.getEffects()) {
                    stringBuilder.append(";").append(effect);
                }
                return stringBuilder.toString();
            }
        }
    }

    public static class EnigmaticRecipe implements IJeiBrewingRecipe {
        private final List<ItemStack> ingredients;
        private final List<ItemStack> potionInputs;
        private final ItemStack potionOutput;
        private final int hashCode;
        private final ResourceLocation uid;
        private final IJeiBrewingRecipe vanillaRecipe;

        public EnigmaticRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput, ResourceLocation uid, IVanillaRecipeFactory factory) {
            this.ingredients = List.copyOf(ingredients);
            this.potionInputs = List.copyOf(potionInputs);
            this.potionOutput = potionOutput;
            this.uid = uid;
            this.vanillaRecipe = factory.createBrewingRecipe(ingredients, potionInputs, potionOutput, uid);
            if (uid != null) {
                this.hashCode = uid.hashCode();
            } else {
                this.hashCode = Objects.hash(ingredients.stream().map(ItemStack::getItem).toList(), potionInputs.stream().map(ItemStack::getItem).toList(), potionOutput.getItem());
            }
        }

        public List<ItemStack> getPotionInputs() {
            return this.potionInputs;
        }

        public List<ItemStack> getIngredients() {
            return this.ingredients;
        }

        public ItemStack getPotionOutput() {
            return this.potionOutput;
        }

        public int getBrewingSteps() {
            return vanillaRecipe.getBrewingSteps();
        }

        public boolean equals(Object obj) {
            if (obj instanceof EnigmaticRecipe other) {
                int i;
                for (i = 0; i < this.potionInputs.size(); ++i) {
                    ItemStack potionInput = this.potionInputs.get(i);
                    ItemStack otherPotionInput = other.potionInputs.get(i);
                    if (arePotionsNotEqual(potionInput, otherPotionInput)) {
                        return false;
                    }
                }

                if (arePotionsNotEqual(other.potionOutput, this.potionOutput)) {
                    return false;
                } else if (this.ingredients.size() != other.ingredients.size()) {
                    return false;
                } else {
                    for (i = 0; i < this.ingredients.size(); ++i) {
                        if (!ItemStack.matches(this.ingredients.get(i), other.ingredients.get(i))) {
                            return false;
                        }
                    }
                    return true;
                }
            } else {
                return false;
            }
        }

        private static boolean arePotionsNotEqual(ItemStack potion1, ItemStack potion2) {
            Item item1 = potion1.getItem();
            Item item2 = potion2.getItem();
            if (item1 != item2) {
                return true;
            } else {
                Potion type1 = PotionUtils.getPotion(potion1);
                Potion type2 = PotionUtils.getPotion(potion2);
                if (item1 instanceof IAdvancedPotionItem advancedPotion1) {
                    IAdvancedPotionItem advancedPotion2 = (IAdvancedPotionItem) item2;
                    if (advancedPotion1.getPotionType() != advancedPotion2.getPotionType()) return true;
                    if (PotionHelper.getAdvancedPotion(potion1) != PotionHelper.getAdvancedPotion(potion2))
                        return true;
                    return PotionAddonHelper.getAdvancedPotion(potion1) != PotionAddonHelper.getAdvancedPotion(potion2);
                }
                return !Objects.equals(type2, type1);
            }
        }

        public @Nullable ResourceLocation getUid() {
            return this.uid;
        }

        public int hashCode() {
            return this.hashCode;
        }

        public String toString() {
            Potion inputType = PotionUtils.getPotion(this.potionInputs.get(0));
            Potion outputType = PotionUtils.getPotion(this.potionOutput);
            return this.ingredients + " + [" + this.potionInputs.get(0).getItem() + " " + inputType.getName("") + "] = [" + this.potionOutput + " " + outputType.getName("") + "]";
        }
    }

    public static class AstralRecipe implements IJeiBrewingRecipe {
        private final int hashCode;
        private final ResourceLocation uid;

        public AstralRecipe(ResourceLocation uid) {
            this.uid = uid;
            if (uid != null) {
                this.hashCode = uid.hashCode();
            } else {
                this.hashCode = Objects.hash(List.of(EnigmaticBlocks.ASTRAL_BLOCK), List.of(Items.HONEY_BOTTLE), EnigmaticAddonItems.ASTRAL_POTION);
            }
        }

        public List<ItemStack> getPotionInputs() {
            return List.of(new ItemStack(Items.HONEY_BOTTLE));
        }

        public List<ItemStack> getIngredients() {
            return List.of(new ItemStack(EnigmaticBlocks.ASTRAL_BLOCK));
        }

        public ItemStack getPotionOutput() {
            return new ItemStack(EnigmaticAddonItems.ASTRAL_POTION);
        }

        public int hashCode() {
            return this.hashCode;
        }

        public @Nullable ResourceLocation getUid() {
            return this.uid;
        }

        public int getBrewingSteps() {
            return 1;
        }

        public String toString() {
            return "astral_addon_brewing_recipe";
        }
    }
}

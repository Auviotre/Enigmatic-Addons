package auviotre.enigmatic.addon.helpers;

import auviotre.enigmatic.addon.contents.brewing.AddonBrewingRecipe;
import auviotre.enigmatic.addon.contents.entities.UltimatePotionEntity;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.registries.EnigmaticAddonPotions;
import com.aizistral.enigmaticlegacy.entities.EnigmaticPotionEntity;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.objects.AdvancedPotion;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticPotions;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.aizistral.enigmaticlegacy.helpers.PotionHelper.*;

public class PotionAddonHelper {
    public static final List<AdvancedPotion> COMMON_POTIONS = new ArrayList<>();
    public static final List<AdvancedPotion> ULTIMATE_POTIONS = new ArrayList<>();
    public static final Ingredient SPECIAL_POTIONS = Ingredient.of(
            Items.HONEY_BOTTLE,
            EnigmaticAddonItems.ASTRAL_POTION,
            EnigmaticAddonItems.BLESS_POTION,
            EnigmaticAddonItems.COMMON_POTION_LINGERING,
            EnigmaticAddonItems.ULTIMATE_POTION_LINGERING
    );
    private static final Ingredient BLUE_ICE = Ingredient.of(Items.BLUE_ICE);
    private static final Ingredient REDSTONE = Ingredient.of(Items.REDSTONE);
    private static final Ingredient GUNPOWDER = Ingredient.of(Items.GUNPOWDER);
    private static final Ingredient GLOWSTONE_DUST = Ingredient.of(Items.GLOWSTONE_DUST);
    private static final Ingredient DRAGON_BREATH = Ingredient.of(Items.DRAGON_BREATH);
    private static final Ingredient ASTRAL_DUST = Ingredient.of(EnigmaticItems.ASTRAL_DUST);
    private static final Ingredient EARTH_HEART_FRAGMENT = Ingredient.of(EnigmaticAddonItems.EARTH_HEART_FRAGMENT);
    private static final Ingredient FERMENTED_SPIDER_EYE = Ingredient.of(Items.FERMENTED_SPIDER_EYE);

    private static final String defaultPotion = "nothingAddon";

    private static final AbstractProjectileDispenseBehavior potionBehavior = new AbstractProjectileDispenseBehavior() {
        protected Projectile getProjectile(Level worldIn, Position position, ItemStack stackIn) {
            EnigmaticPotionEntity potionEntity = new EnigmaticPotionEntity(worldIn, position.x(), position.y(), position.z());
            potionEntity.setItem(stackIn);
            return potionEntity;
        }
    };
    private static final AbstractProjectileDispenseBehavior addonPotionBehavior = new AbstractProjectileDispenseBehavior() {
        protected Projectile getProjectile(Level worldIn, Position position, ItemStack stackIn) {
            UltimatePotionEntity potionEntity = new UltimatePotionEntity(worldIn, position.x(), position.y(), position.z());
            potionEntity.setItem(stackIn);
            return potionEntity;
        }
    };

    public static void addCommonPotion(AdvancedPotion... potions) {
        COMMON_POTIONS.addAll(Arrays.asList(potions));
    }

    public static void addUltimatePotion(AdvancedPotion... potions) {
        ULTIMATE_POTIONS.addAll(Arrays.asList(potions));
    }

    public static void registerCommonPotions() {
        addCommonPotion(EnigmaticAddonPotions.FROZEN_HEART, EnigmaticAddonPotions.LONG_FROZEN_HEART, EnigmaticAddonPotions.MINING_FATIGUE, EnigmaticAddonPotions.LONG_MINING_FATIGUE, EnigmaticAddonPotions.STRONG_MINING_FATIGUE);
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createVanillaPotion(Items.POTION, Potions.AWKWARD)), EARTH_HEART_FRAGMENT), createVanillaPotion(Items.POTION, Potions.LUCK)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createVanillaPotion(Items.SPLASH_POTION, Potions.AWKWARD)), EARTH_HEART_FRAGMENT), createVanillaPotion(Items.SPLASH_POTION, Potions.LUCK)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createVanillaPotion(Items.LINGERING_POTION, Potions.AWKWARD)), EARTH_HEART_FRAGMENT), createVanillaPotion(Items.LINGERING_POTION, Potions.LUCK)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createVanillaPotion(Items.POTION, Potions.MUNDANE)), BLUE_ICE), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.FROZEN_HEART)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.FROZEN_HEART)), REDSTONE), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.LONG_FROZEN_HEART)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createVanillaPotion(Items.SPLASH_POTION, Potions.MUNDANE)), BLUE_ICE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.FROZEN_HEART)), GUNPOWDER), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.FROZEN_HEART)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.FROZEN_HEART)), REDSTONE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.LONG_FROZEN_HEART)), GUNPOWDER), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.LONG_FROZEN_HEART)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createVanillaPotion(Items.LINGERING_POTION, Potions.MUNDANE)), BLUE_ICE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.FROZEN_HEART)), DRAGON_BREATH), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.FROZEN_HEART)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.FROZEN_HEART)), REDSTONE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.LONG_FROZEN_HEART)), DRAGON_BREATH), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.LONG_FROZEN_HEART)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.HASTE)), FERMENTED_SPIDER_EYE), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.MINING_FATIGUE)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.LONG_HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.MINING_FATIGUE)), REDSTONE), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.LONG_MINING_FATIGUE)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.STRONG_HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.MINING_FATIGUE)), GLOWSTONE_DUST), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.STRONG_MINING_FATIGUE)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.MINING_FATIGUE)), GUNPOWDER), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.MINING_FATIGUE)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.LONG_HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.LONG_MINING_FATIGUE)), GUNPOWDER, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.MINING_FATIGUE)), REDSTONE), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.LONG_MINING_FATIGUE)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.STRONG_HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.STRONG_MINING_FATIGUE)), GUNPOWDER, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.MINING_FATIGUE)), GLOWSTONE_DUST), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.STRONG_MINING_FATIGUE)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.MINING_FATIGUE)), DRAGON_BREATH), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.MINING_FATIGUE)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.LONG_HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.LONG_MINING_FATIGUE)), DRAGON_BREATH, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.MINING_FATIGUE)), REDSTONE), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.LONG_MINING_FATIGUE)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.STRONG_HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.STRONG_MINING_FATIGUE)), DRAGON_BREATH, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.MINING_FATIGUE)), GLOWSTONE_DUST), createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.STRONG_MINING_FATIGUE)));
    }

    public static void registerUltimatePotions() {
        addUltimatePotion(EnigmaticAddonPotions.ULTIMATE_FROZEN_HEART, EnigmaticAddonPotions.ULTIMATE_MINING_FATIGUE);
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.LONG_FROZEN_HEART)), ASTRAL_DUST), createAdvancedPotion(EnigmaticAddonItems.ULTIMATE_POTION, EnigmaticAddonPotions.ULTIMATE_FROZEN_HEART)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.LONG_FROZEN_HEART)), ASTRAL_DUST, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.ULTIMATE_POTION, EnigmaticAddonPotions.ULTIMATE_FROZEN_HEART)), GUNPOWDER), createAdvancedPotion(EnigmaticAddonItems.ULTIMATE_POTION_SPLASH, EnigmaticAddonPotions.ULTIMATE_FROZEN_HEART)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.LONG_FROZEN_HEART)), ASTRAL_DUST, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.ULTIMATE_POTION_SPLASH, EnigmaticAddonPotions.ULTIMATE_FROZEN_HEART)), DRAGON_BREATH), createAdvancedPotion(EnigmaticAddonItems.ULTIMATE_POTION_LINGERING, EnigmaticAddonPotions.ULTIMATE_FROZEN_HEART)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.LONG_MINING_FATIGUE)), ASTRAL_DUST, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION, EnigmaticAddonPotions.STRONG_MINING_FATIGUE)), ASTRAL_DUST), createAdvancedPotion(EnigmaticAddonItems.ULTIMATE_POTION, EnigmaticAddonPotions.ULTIMATE_MINING_FATIGUE)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.LONG_MINING_FATIGUE)), ASTRAL_DUST, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_SPLASH, EnigmaticAddonPotions.STRONG_MINING_FATIGUE)), ASTRAL_DUST, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.ULTIMATE_POTION, EnigmaticAddonPotions.ULTIMATE_MINING_FATIGUE)), GUNPOWDER), createAdvancedPotion(EnigmaticAddonItems.ULTIMATE_POTION_SPLASH, EnigmaticAddonPotions.ULTIMATE_MINING_FATIGUE)));
        BrewingRecipeRegistry.addRecipe(new AddonBrewingRecipe(constructIngredientMap(Ingredient.of(createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_HASTE)), FERMENTED_SPIDER_EYE, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.LONG_MINING_FATIGUE)), ASTRAL_DUST, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.COMMON_POTION_LINGERING, EnigmaticAddonPotions.STRONG_MINING_FATIGUE)), ASTRAL_DUST, Ingredient.of(createAdvancedPotion(EnigmaticAddonItems.ULTIMATE_POTION_SPLASH, EnigmaticAddonPotions.ULTIMATE_MINING_FATIGUE)), DRAGON_BREATH), createAdvancedPotion(EnigmaticAddonItems.ULTIMATE_POTION_LINGERING, EnigmaticAddonPotions.ULTIMATE_MINING_FATIGUE)));
    }

    public static void registerDispenserBehavior() {
        DispenserBlock.registerBehavior(EnigmaticItems.COMMON_POTION_SPLASH, potionBehavior);
        DispenserBlock.registerBehavior(EnigmaticItems.COMMON_POTION_LINGERING, potionBehavior);
        DispenserBlock.registerBehavior(EnigmaticAddonItems.COMMON_POTION_SPLASH, addonPotionBehavior);
        DispenserBlock.registerBehavior(EnigmaticAddonItems.COMMON_POTION_LINGERING, addonPotionBehavior);
        DispenserBlock.registerBehavior(EnigmaticItems.ULTIMATE_POTION_SPLASH, potionBehavior);
        DispenserBlock.registerBehavior(EnigmaticItems.ULTIMATE_POTION_LINGERING, potionBehavior);
        DispenserBlock.registerBehavior(EnigmaticAddonItems.ULTIMATE_POTION_SPLASH, addonPotionBehavior);
        DispenserBlock.registerBehavior(EnigmaticAddonItems.ULTIMATE_POTION_LINGERING, addonPotionBehavior);
    }

    public static int getColor(ItemStack stack) {
        return isAdvancedPotion(stack) && getEffects(stack) != null && !getEffects(stack).isEmpty() ? PotionUtils.getColor(getEffects(stack)) : PotionUtils.getColor(stack);
    }

    public static AdvancedPotion getAdvancedPotion(ItemStack stack) {
        return isAdvancedPotion(stack) ? getAdvancedPotion(ItemNBTHelper.getString(stack, "EnigmaticPotion", defaultPotion)) : EnigmaticPotions.EMPTY_POTION;
    }

    public static List<MobEffectInstance> getEffects(ItemStack stack) {
        if (isAdvancedPotion(stack)) {
            AdvancedPotion potion = getAdvancedPotion(ItemNBTHelper.getString(stack, "EnigmaticPotion", defaultPotion));
            if (potion != null) return potion.getEffects();
        }
        return new ArrayList<>();
    }

    public static AdvancedPotion getAdvancedPotion(String identifier) {
        Iterator<AdvancedPotion> iterator = ULTIMATE_POTIONS.iterator();

        AdvancedPotion potion;
        do {
            if (!iterator.hasNext()) {
                iterator = COMMON_POTIONS.iterator();
                do {
                    if (!iterator.hasNext()) {
                        return EnigmaticPotions.EMPTY_POTION;
                    }
                    potion = iterator.next();
                } while (!potion.getId().equals(identifier));

                return potion;
            }
            potion = iterator.next();
        } while (!potion.getId().equals(identifier));
        return potion;
    }
}

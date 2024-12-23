package auviotre.enigmatic.addon.contents.crafting;

import auviotre.enigmatic.addon.contents.items.DragonBow;
import auviotre.enigmatic.addon.contents.items.UltimatePotionAddon;
import auviotre.enigmatic.addon.helpers.PotionAddonHelper;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;
import com.aizistral.enigmaticlegacy.items.UltimatePotionSplash;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class DragonBowBrewingRecipe extends CustomRecipe {
    public static final SimpleCraftingRecipeSerializer<DragonBowBrewingRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(DragonBowBrewingRecipe::new);

    public DragonBowBrewingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    public ItemStack assemble(CraftingContainer inventory, RegistryAccess access) {
        ItemStack bow = null;
        boolean hasWater = false;
        List<MobEffectInstance> effects = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack checkedStack = inventory.getItem(i);
            if (!checkedStack.isEmpty()) {
                if (checkedStack.getItem() instanceof DragonBow) {
                    if (bow != null) return ItemStack.EMPTY;
                    bow = checkedStack.copy();
                } else if (checkedStack.getItem() instanceof SplashPotionItem) {
                    if (PotionUtils.getPotion(checkedStack) == Potions.WATER) hasWater = true;
                    effects.addAll(PotionUtils.getPotion(checkedStack).getEffects());
                } else if (checkedStack.getItem() instanceof UltimatePotionSplash) {
                    effects.addAll(PotionHelper.getEffects(checkedStack));
                } else if (checkedStack.getItem() instanceof UltimatePotionAddon.Splash) {
                    effects.addAll(PotionAddonHelper.getEffects(checkedStack));
                }
            }
        }
        if (bow == null) return ItemStack.EMPTY;
        if (hasWater) {
            DragonBow.resetEffect(bow);
            return bow;
        }
        for (MobEffectInstance effect : effects) {
            DragonBow.addEffect(bow, effect);
        }
        return effects.isEmpty() ? ItemStack.EMPTY : bow;
    }

    public boolean matches(CraftingContainer inventory, Level world) {
        ItemStack bow = null;
        List<ItemStack> potions = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack checkedStack = inventory.getItem(i);
            if (!checkedStack.isEmpty()) {
                if (checkedStack.getItem() instanceof DragonBow) {
                    if (bow != null) return false;
                    bow = checkedStack.copy();
                } else if (checkedStack.getItem() instanceof SplashPotionItem) {
                    potions.add(checkedStack);
                } else if (checkedStack.getItem() instanceof UltimatePotionSplash) {
                    potions.add(checkedStack);
                } else if (checkedStack.getItem() instanceof UltimatePotionAddon.Splash) {
                    potions.add(checkedStack);
                }
            }
        }
        return bow != null && !potions.isEmpty() && potions.toArray().length <= DragonBow.maxPotionAmount.getValue();
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

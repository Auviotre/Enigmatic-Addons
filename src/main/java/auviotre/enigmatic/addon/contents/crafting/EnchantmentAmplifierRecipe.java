package auviotre.enigmatic.addon.contents.crafting;

import auviotre.enigmatic.addon.contents.items.BlessAmplifier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class EnchantmentAmplifierRecipe extends CustomRecipe {
    public static final SimpleCraftingRecipeSerializer<EnchantmentAmplifierRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(EnchantmentAmplifierRecipe::new);

    public EnchantmentAmplifierRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        List<ItemStack> stackList = new ArrayList<>();
        ItemStack amplifier = null;

        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack checkedStack = craftingContainer.getItem(i);
            if (!checkedStack.isEmpty()) {
                if (checkedStack.getItem() instanceof BlessAmplifier) {
                    if (amplifier != null) return ItemStack.EMPTY;
                    amplifier = checkedStack.copy();
                } else {
                    stackList.add(checkedStack);
                }
            }
        }

        if (amplifier != null && stackList.size() == 1 && stackList.get(0).isEnchanted() && this.canBoost(amplifier, stackList.get(0))) {
            return this.boost(stackList.get(0));
        } else {
            return ItemStack.EMPTY;
        }
    }


    public boolean matches(CraftingContainer craftingContainer, Level level) {
        List<ItemStack> stackList = new ArrayList<>();
        ItemStack amplifier = null;

        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack checkedStack = craftingContainer.getItem(i);
            if (!checkedStack.isEmpty()) {
                if (checkedStack.getItem() instanceof BlessAmplifier) {
                    if (amplifier != null) return false;
                    amplifier = checkedStack.copy();
                } else {
                    stackList.add(checkedStack);
                }
            }
        }
        return amplifier != null && stackList.size() == 1 && stackList.get(0).isEnchanted() && this.canBoost(amplifier, stackList.get(0));
    }

    private ItemStack boost(ItemStack target) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(target);
        for (Enchantment enchantment : enchantments.keySet()) {
            int maxLevel = Mth.ceil(enchantment.getMaxLevel() * 1.5);
            if (enchantment.getMaxLevel() > 1 && maxLevel > target.getEnchantmentLevel(enchantment)) {
                enchantments.compute(enchantment, (enchant, integer) -> Math.min(integer * 2, maxLevel));
            }
        }
        ItemStack item = target.copy();
        EnchantmentHelper.setEnchantments(enchantments, item);
        return item;
    }

    private boolean canBoost(ItemStack amplifier, ItemStack target) {
        Stream<Enchantment> enchantments = EnchantmentHelper.getEnchantments(target).keySet().stream();
        Objects.requireNonNull(amplifier.getItem());
        return enchantments.anyMatch(enchantment -> enchantment.getMaxLevel() > 1 && Mth.ceil(enchantment.getMaxLevel() * 1.5) > target.getEnchantmentLevel(enchantment));
    }


    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

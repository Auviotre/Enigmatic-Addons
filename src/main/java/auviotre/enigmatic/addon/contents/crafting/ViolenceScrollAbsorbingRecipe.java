package auviotre.enigmatic.addon.contents.crafting;

import auviotre.enigmatic.addon.contents.items.ViolenceScroll;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Stream;

public class ViolenceScrollAbsorbingRecipe extends CustomRecipe {
    public static final SimpleCraftingRecipeSerializer<ViolenceScrollAbsorbingRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(ViolenceScrollAbsorbingRecipe::new);

    public ViolenceScrollAbsorbingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        List<ItemStack> stackList = new ArrayList<>();
        ItemStack scroll = null;

        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack checkedStack = craftingContainer.getItem(i);
            if (!checkedStack.isEmpty()) {
                if (checkedStack.getItem() instanceof ViolenceScroll) {
                    if (scroll != null) return ItemStack.EMPTY;
                    scroll = checkedStack.copy();
                } else {
                    stackList.add(checkedStack);
                }
            }
        }

        if (scroll != null && stackList.size() == 1 && stackList.get(0).isEnchanted() && this.canDisenchant(scroll, stackList.get(0))) {
            return this.disenchant(scroll, stackList.get(0)).getA();
        } else {
            return ItemStack.EMPTY;
        }
    }


    public boolean matches(CraftingContainer craftingContainer, Level level) {
        List<ItemStack> stackList = new ArrayList<>();
        ItemStack scroll = null;

        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack checkedStack = craftingContainer.getItem(i);
            if (!checkedStack.isEmpty()) {
                if (checkedStack.getItem() instanceof ViolenceScroll) {
                    if (scroll != null) return false;
                    scroll = checkedStack.copy();
                } else {
                    stackList.add(checkedStack);
                }
            }
        }
        return scroll != null && stackList.size() == 1 && stackList.get(0).isEnchanted() && this.canDisenchant(scroll, stackList.get(0));
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        Map<ItemStack, Integer> stackList = new HashMap<>();
        ItemStack scroll = null;
        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack checkedItemStack = inv.getItem(i);
            if (!checkedItemStack.isEmpty()) {
                if (checkedItemStack.getItem() instanceof ViolenceScroll) {
                    if (scroll != null) return remaining;
                    scroll = checkedItemStack.copy();
                } else stackList.put(checkedItemStack, i);
            }
        }
        if (scroll != null && stackList.size() == 1) {
            ItemStack returned = stackList.keySet().iterator().next();
            if (returned.isEnchanted() && this.canDisenchant(scroll, returned)) {
                remaining.set(stackList.get(returned), this.disenchant(scroll, returned).getB());
            }
        }
        return remaining;
    }

    private Tuple<ItemStack, ItemStack> disenchant(ItemStack transposer, ItemStack target) {
        Map<Enchantment, Integer> transposed = EnchantmentHelper.getEnchantments(target);
        Map<Enchantment, Integer> leftover = EnchantmentHelper.getEnchantments(target);
        transposed.keySet().removeIf(enchantment -> !enchantment.isCurse());
        leftover.keySet().removeIf(Enchantment::isCurse);
        ItemStack scroll = transposer.copy();
        ViolenceScroll.Helper.addCurse(scroll, transposed.keySet());
        ItemStack item = target.copy();
        EnchantmentHelper.setEnchantments(leftover, item);
        return new Tuple<>(scroll, item);
    }

    private boolean canDisenchant(ItemStack transposer, ItemStack target) {
        Stream<Enchantment> stream = EnchantmentHelper.getEnchantments(target).keySet().stream();
        Objects.requireNonNull(transposer.getItem());
        return stream.anyMatch(Enchantment::isCurse);
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

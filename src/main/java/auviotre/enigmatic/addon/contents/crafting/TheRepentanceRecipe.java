package auviotre.enigmatic.addon.contents.crafting;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public class TheRepentanceRecipe extends CustomRecipe {
    public static final SimpleCraftingRecipeSerializer<TheRepentanceRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(TheRepentanceRecipe::new);

    public TheRepentanceRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    public ItemStack assemble(CraftingContainer inventory, RegistryAccess access) {
        int soulCount = 0;
        int hasBless = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack slotStack = inventory.getItem(i);
            if (!slotStack.isEmpty()) {
                if (slotStack.is(EnigmaticAddonItems.THE_BLESS)) hasBless++;
                if (slotStack.is(EnigmaticItems.SOUL_CRYSTAL)) soulCount++;
            }
        }
        ItemStack book = EnigmaticAddonItems.THE_REPENTANCE.getDefaultInstance();
        if (soulCount > 3 && hasBless == 1)
            return book;
        return ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> list = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for(int i = 0; i < list.size(); ++i) {
            ItemStack item = container.getItem(i);
            if (item.is(EnigmaticItems.SOUL_CRYSTAL)) list.set(i, item.copy());
        }
        return list;
    }

    public boolean matches(CraftingContainer inventory, Level world) {
        int soulCount = 0;
        int hasBless = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack slotStack = inventory.getItem(i);
            if (!slotStack.isEmpty()) {
                if (slotStack.is(EnigmaticAddonItems.THE_BLESS)) hasBless++;
                if (slotStack.is(EnigmaticItems.SOUL_CRYSTAL)) soulCount++;
            }
        }
        return soulCount > 3 && hasBless == 1;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

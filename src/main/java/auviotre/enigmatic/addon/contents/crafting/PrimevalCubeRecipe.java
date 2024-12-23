package auviotre.enigmatic.addon.contents.crafting;

import auviotre.enigmatic.addon.handlers.OmniconfigAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class PrimevalCubeRecipe extends CustomRecipe {
    public static final SimpleCraftingRecipeSerializer<PrimevalCubeRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(PrimevalCubeRecipe::new);

    public PrimevalCubeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    public ItemStack assemble(CraftingContainer inventory, RegistryAccess access) {
        int spellstoneCount = 0;
        int goldCount = 0;
        int heartCount = 0;

        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack slotStack = inventory.getItem(i);
            if (!slotStack.isEmpty()) {
                if (EnigmaticItems.SPELLSTONES.contains(slotStack.getItem())) spellstoneCount++;
                if (slotStack.is(Blocks.GOLD_BLOCK.asItem())) goldCount++;
                if (slotStack.is(EnigmaticItems.EARTH_HEART)) heartCount++;
                if (slotStack.is(Items.HEART_OF_THE_SEA)) heartCount += 128;
            }
        }
        if (spellstoneCount == 6 && goldCount == 1 && heartCount == 129 && OmniconfigAddonHandler.isItemEnabled(EnigmaticAddonItems.PRIMEVAL_CUBE))
            return new ItemStack(EnigmaticAddonItems.PRIMEVAL_CUBE);
        return ItemStack.EMPTY;
    }


    public boolean matches(CraftingContainer inventory, Level world) {
        int spellstoneCount = 0;
        int goldCount = 0;
        int heartCount = 0;

        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack slotStack = inventory.getItem(i);
            if (!slotStack.isEmpty()) {
                if (EnigmaticItems.SPELLSTONES.contains(slotStack.getItem())) spellstoneCount++;
                if (slotStack.is(Blocks.GOLD_BLOCK.asItem())) goldCount++;
                if (slotStack.is(EnigmaticItems.EARTH_HEART)) heartCount++;
                if (slotStack.is(Items.HEART_OF_THE_SEA)) heartCount += 128;
            }
        }
        return spellstoneCount == 6 && goldCount == 1 && heartCount == 129 && OmniconfigAddonHandler.isItemEnabled(EnigmaticAddonItems.PRIMEVAL_CUBE);
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.EnigmaticAddons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnigmaticAddonBlocks extends AbstractRegistry<Block> {
    private static final Map<ResourceLocation, BlockItemSupplier> BLOCK_ITEM_MAP = new HashMap<>();
    private static final EnigmaticAddonBlocks INSTANCE = new EnigmaticAddonBlocks();

    /*
     *  @ObjectHolder(value = "enigmaticaddons:_", registryName = "block")
     *  public static final _ _ = null;
     */
    protected EnigmaticAddonBlocks() {
        super(ForgeRegistries.BLOCKS);
    }

    protected static Map<ResourceLocation, BlockItemSupplier> getBlockItemMap() {
        return Collections.unmodifiableMap(BLOCK_ITEM_MAP);
    }

    protected void register(String name, Supplier<Block> block, BlockItemSupplier item) {
        super.register(name, block);
        BLOCK_ITEM_MAP.put(new ResourceLocation(EnigmaticAddons.MODID, name), item);
    }

    @FunctionalInterface
    protected interface BlockItemSupplier extends Function<Block, BlockItem> {
        BlockItem apply(Block block);
    }
}

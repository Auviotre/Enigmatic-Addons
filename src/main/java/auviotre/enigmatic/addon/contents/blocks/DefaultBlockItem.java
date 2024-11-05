package auviotre.enigmatic.addon.contents.blocks;

import com.aizistral.enigmaticlegacy.api.items.ICreativeTabMember;
import com.aizistral.enigmaticlegacy.items.generic.GenericBlockItem;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DefaultBlockItem extends BlockItem implements ICreativeTabMember {
    private final Supplier<CreativeModeTab> tab;
    private boolean foil = false;

    public DefaultBlockItem(Block block, Properties properties, Supplier<@Nullable CreativeModeTab> tab) {
        super(block, properties);
        this.tab = tab;
    }

    public DefaultBlockItem(Block block, Properties properties) {
        this(block, properties, () -> EnigmaticTabs.MAIN);
    }

    public DefaultBlockItem(Block blockIn) {
        this(blockIn, GenericBlockItem.getDefaultProperties());
    }

    public DefaultBlockItem(Block blockIn, Rarity rarity) {
        this(blockIn, GenericBlockItem.getDefaultProperties().rarity(rarity));
    }

    public DefaultBlockItem(Block blockIn, Rarity rarity, boolean foil) {
        this(blockIn, GenericBlockItem.getDefaultProperties().rarity(rarity));
        this.foil = foil;
    }

    public boolean isFoil(ItemStack stack) {
        return this.foil;
    }

    public CreativeModeTab getCreativeTab() {
        return this.tab.get();
    }
}

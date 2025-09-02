package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class TheRepentance extends ItemBase {
    public TheRepentance() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC).fireResistant());
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int id, boolean selected) {
        stack.shrink(1);
    }

    public CreativeModeTab getCreativeTab() {
        return null;
    }
}

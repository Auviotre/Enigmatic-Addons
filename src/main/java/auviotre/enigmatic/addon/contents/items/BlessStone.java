package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.world.item.Rarity;

public class BlessStone extends ItemBase {
    public BlessStone() {
        super(ItemBase.getDefaultProperties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }
}

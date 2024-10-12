package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BlessStone extends ItemBase implements ICursed {
    public BlessStone() {
        super(ItemBase.getDefaultProperties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessStone1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessStone2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessStone3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedStone6");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }
}

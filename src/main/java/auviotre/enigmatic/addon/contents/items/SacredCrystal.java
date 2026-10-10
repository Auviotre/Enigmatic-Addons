package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBetrayed;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class SacredCrystal extends ItemBase implements ICursed, IBetrayed {
    public SacredCrystal() {
        super(ItemBase.getDefaultProperties().rarity(Rarity.RARE));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> list, TooltipFlag flag) {
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }
}

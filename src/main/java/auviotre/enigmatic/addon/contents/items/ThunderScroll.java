package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBlessed;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

public class ThunderScroll extends ItemBaseCurio implements ICursed, IBlessed {

    public ThunderScroll() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC));
    }

    public static float modify(LivingEntity target, float damage) {
        if (target.getAttributes().hasAttribute(Attributes.ARMOR)) {
            double value = target.getAttribute(Attributes.ARMOR).getValue();
            if (value > 0) {
                double factor = 1.0 - Math.min(0.0375 * value, 0.75);
                damage = (float) (damage / Math.sqrt(factor));
            }
        }
        return damage;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll5");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (super.canEquip(context, stack)) {
            LivingEntity entity = context.entity();
            return entity instanceof Player player && SuperAddonHandler.isOKOne(player);
        }
        return false;
    }
}

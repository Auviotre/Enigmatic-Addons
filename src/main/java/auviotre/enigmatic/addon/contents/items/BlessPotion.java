package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBetrayed;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBasePotion;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlessPotion extends ItemBasePotion implements ICursed, IBetrayed {
    public BlessPotion() {
        super(getDefaultProperties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessPotion1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessPotion2");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public void onConsumed(Level world, Player player, ItemStack potion) {
        int removeCount = 0;
        List<MobEffectInstance> activeEffects = new ArrayList<>(player.getActiveEffects());
        for (MobEffectInstance effect : activeEffects) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                if (player.removeEffect(effect.getEffect())) removeCount++;
            }
        }
        if (SuperAddonHandler.isTheBlessedOne(player))
            player.addEffect(new MobEffectInstance(EnigmaticAddonEffects.PURE_RESISTANCE_EFFECT, 1800 + 480 * removeCount, 1));
        else player.addEffect(new MobEffectInstance(EnigmaticAddonEffects.PURE_RESISTANCE_EFFECT, 1200 + 300 * removeCount, 1));
    }

    public boolean canDrink(Level world, Player player, ItemStack potion) {
        return SuperAddonHandler.isOKOne(player);
    }
}

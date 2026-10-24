package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBlessed;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.UUID;

public class ForgerCrystal extends ItemBaseCurio implements ICursed, IBlessed {
    public ForgerCrystal() {
        super(getDefaultProperties().rarity(Rarity.EPIC).fireResistant().stacksTo(1));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enchantersPearl1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgerGem2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgerGem3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgerGem4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgerGem5");
            if (Minecraft.getInstance().player != null && BlessRing.Helper.blessAvailable(Minecraft.getInstance().player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgerCrystal");
            }
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        if (slotContext.entity() instanceof Player) {
            CuriosApi.addSlotModifier(attributes, "charm", UUID.fromString("4f9d6bf4-49b5-6543-8796-b0c75e53aa91"), 1.0, AttributeModifier.Operation.ADDITION);
        }
        return attributes;
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }

    public boolean isPresent(Player player) {
        return SuperAddonHandler.isOKOne(player) && SuperpositionHandler.hasCurio(player, this);
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player) {
            return super.canEquip(context, stack) && !SuperpositionHandler.hasCurio(context.entity(), EnigmaticAddonItems.FORGER_GEM);
        }
        return false;
    }
}

package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.triggers.CursedRingEquippedTrigger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class BlessRing extends ItemBaseCurio {
    public BlessRing() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC));
    }


    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing3", ChatFormatting.GOLD, (CursedRing.lootingBonus.getValue() + 1) / 2);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing4", ChatFormatting.GOLD, (CursedRing.fortuneBonus.getValue() + 1) / 2);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing6");
        } else {
            if (CursedRing.enableLore.getValue()) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore1");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore2");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore3");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore4");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            }

            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.eternallyBound1");
            if (Minecraft.getInstance().player != null && SuperpositionHandler.canUnequipBoundRelics(Minecraft.getInstance().player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.eternallyBound2_creative");
            } else {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.eternallyBound2");
            }
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }

    public boolean canUnequip(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            if (SuperpositionHandler.canUnequipBoundRelics(player)) {
                return super.canUnequip(context, stack);
            }
        }
        return false;
    }

    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return false;
    }

    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        SuperpositionHandler.setCurrentWorldCursed(false);
    }

    public void onEquip(SlotContext context, ItemStack prevStack, ItemStack stack) {
        if (context.entity() instanceof ServerPlayer player) {
            CursedRingEquippedTrigger.INSTANCE.trigger(player);
        }
        SuperpositionHandler.setCurrentWorldCursed(true);
    }

    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return ICurio.DropRule.ALWAYS_KEEP;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        Map<Enchantment, Integer> list = EnchantmentHelper.getEnchantments(book);
        return !list.containsKey(Enchantments.VANISHING_CURSE) && super.isBookEnchantable(stack, book);
    }

    public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack curio) {
        return super.getFortuneLevel(slotContext, lootContext, curio) + (CursedRing.fortuneBonus.getValue() + 1) / 2;
    }

    public int getLootingLevel(SlotContext slotContext, DamageSource source, LivingEntity target, int baseLooting, ItemStack curio) {
        return super.getLootingLevel(slotContext, source, target, baseLooting, curio) + (CursedRing.lootingBonus.getValue() + 1) / 2;
    }
}

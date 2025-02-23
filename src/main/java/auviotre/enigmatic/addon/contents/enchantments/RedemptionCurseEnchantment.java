package auviotre.enigmatic.addon.contents.enchantments;

import auviotre.enigmatic.addon.handlers.OmniconfigAddonHandler;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class RedemptionCurseEnchantment extends Enchantment {
    public static Omniconfig.PerhapsParameter resistanceModifier;

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("CurseofRedemptionEnchantment");
        resistanceModifier = builder.comment("The damage modifier per every this curse.").max(40).min(0).getPerhaps("ResistanceModifier", 4);
        builder.popPrefix();
    }
    public RedemptionCurseEnchantment(Rarity rarity, EquipmentSlot[] slots) {
        super(rarity, EnchantmentCategory.ARMOR, slots);
    }

    public int getMinCost(int enchantmentLevel) {
        return 25;
    }

    public int getMaxCost(int enchantmentLevel) {
        return 50;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 1;
    }

    public boolean canEnchant(ItemStack stack) {
        return OmniconfigHandler.isItemEnabled(this) && (super.canEnchant(stack) && SuperpositionHandler.getCurseAmount(stack) == 0);
    }

    public boolean isTreasureOnly() {
        return true;
    }

    public boolean isCurse() {
        return true;
    }

    public boolean isAllowedOnBooks() {
        return OmniconfigAddonHandler.isItemEnabled(this);
    }

    public boolean isDiscoverable() {
        return OmniconfigAddonHandler.isItemEnabled(this);
    }

    protected boolean checkCompatibility(Enchantment enchantment) {
        return !enchantment.isCurse() && super.checkCompatibility(enchantment);
    }

    public Component getFullname(int i) {
        MutableComponent mutablecomponent;
        Player player = EnigmaticLegacy.PROXY.getClientPlayer();
        if (player != null && SuperAddonHandler.isTheBlessedOne(player)) {
            mutablecomponent = Component.translatable(this.getDescriptionId() + "_alt");
            mutablecomponent.withStyle(ChatFormatting.GOLD);
        } else {
            mutablecomponent = Component.translatable(this.getDescriptionId());
            mutablecomponent.withStyle(ChatFormatting.RED);
        }
        return mutablecomponent;
    }

    public static float modify(LivingEntity entity) {
        int amount = 0;
        for (ItemStack armor : entity.getArmorSlots()) {
            amount += SuperAddonHandler.getRedemptionCurseAmount(armor);
        }
        if (amount > 0) {
            if (entity instanceof Player player && SuperAddonHandler.isTheBlessedOne(player)) amount = -amount;
            return resistanceModifier.getValue().asModifier() * amount;
        }
        return 0;
    }
}

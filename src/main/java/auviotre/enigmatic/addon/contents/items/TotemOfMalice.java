package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TotemOfMalice extends ItemBaseCurio implements ICursed {
    public static final List<ResourceLocation> extraRaiderList = new ArrayList<>();
    public static Omniconfig.DoubleParameter raiderBoost;
    public static Omniconfig.DoubleParameter raiderResistance;

    public TotemOfMalice() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC).fireResistant());
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("TotemofMalice");
        raiderBoost = builder.comment("The damage modifier to the Raiders.").max(400).min(0).getDouble("RaiderDamageBoost", 1.5);
        raiderResistance = builder.comment("The damage resistance to the Raiders.").max(400).min(0).getDouble("RaiderDamageResistance", 0.5);
        extraRaiderList.clear();
        String[] list = builder.config.getStringList("TotemofMaliceExtraRaiderList", "Balance Options", new String[0], "List of entities that will be affected as Raider by the Totem of Malice. Examples: minecraft:witch. Changing this option required game restart to take effect.");
        Arrays.stream(list).forEach((entry) -> extraRaiderList.add(new ResourceLocation(entry)));
        builder.popPrefix();
    }

    public static ItemStack getValidTotem(Player player) {
        for (NonNullList<ItemStack> stacks : player.getInventory().compartments) {
            for (ItemStack itemstack : stacks) {
                if (!itemstack.isEmpty() && itemstack.is(EnigmaticAddonItems.TOTEM_OF_MALICE) && isPowerful(itemstack)) {
                    return itemstack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isEnable(Player player, Boolean needUnbroken) {
        if (!SuperpositionHandler.isTheCursedOne(player)) return false;
        ItemStack totem;
        if (needUnbroken) {
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.TOTEM_OF_MALICE)) {
                totem = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.TOTEM_OF_MALICE);
                return isPowerful(totem);
            } else return !getValidTotem(player).isEmpty();
        } else {
            if (SuperpositionHandler.hasItem(player, EnigmaticAddonItems.TOTEM_OF_MALICE)) {
                return true;
            } else return SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.TOTEM_OF_MALICE);
        }
    }

    public static void hurtAndBreak(ItemStack stack, LivingEntity entity) {
        if (!entity.level().isClientSide() && (!(entity instanceof Player) || !((Player) entity).getAbilities().instabuild)) {
            entity.invulnerableTime = 60;
            setTotemPower(stack, getTotemPower(stack) - 1);
        }
    }

    public static boolean isPowerful(ItemStack stack) {
        if (stack.isEmpty()) return false;
        int damage = getTotemPower(stack);
        return damage > 0;
    }

    public static int getTotemPower(ItemStack stack) {
        return !stack.hasTag() ? 0 : stack.getTag().getInt("MalicePower");
    }

    public static void setTotemPower(ItemStack stack, int damage) {
        int level = stack.getEnchantmentLevel(Enchantments.UNBREAKING);
        stack.getOrCreateTag().putInt("MalicePower", Mth.clamp(damage, 0, level + 3));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice2", ChatFormatting.GOLD, String.format("%.0f", 100 * raiderBoost.getValue()) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice3", ChatFormatting.GOLD, String.format("%.0f", 100 * raiderResistance.getValue()) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            if (isPowerful(stack)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice4");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice5");
            } else {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice4_alt");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice5_alt");
            }
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public boolean isFoil(ItemStack stack) {
        return isPowerful(stack);
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING;
    }

    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    public int getBarWidth(ItemStack stack) {
        int level = stack.getEnchantmentLevel(Enchantments.UNBREAKING);
        return Math.round((float) getTotemPower(stack) * 13.0F / (3.0F + level));
    }

    public int getBarColor(ItemStack stack) {
        int level = stack.getEnchantmentLevel(Enchantments.UNBREAKING);
        float stackMaxDamage = 3.0F + level;
        float f = Math.max(0.0F, getTotemPower(stack) / stackMaxDamage);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 0.5F + f * 0.5F);
    }
}

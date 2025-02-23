package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
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
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC));
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

    public static boolean isEnable(Player player) {
        if (!SuperpositionHandler.isTheCursedOne(player)) return false;
        ItemStack totem = null;
        if (SuperpositionHandler.hasItem(player, EnigmaticAddonItems.TOTEM_OF_MALICE)) {
            totem = SuperAddonHandler.getItem(player, EnigmaticAddonItems.TOTEM_OF_MALICE);
        } else if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.TOTEM_OF_MALICE)) {
            totem = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.TOTEM_OF_MALICE);
        }
        return isNotBroken(totem);
    }

    public static void hurtAndBreak(ItemStack stack, LivingEntity entity) {
        if (!entity.level().isClientSide() && (!(entity instanceof Player) || !((Player) entity).getAbilities().instabuild)) {
            int damage = getTotemDamage(stack);
            int level = stack.getEnchantmentLevel(Enchantments.UNBREAKING);
            damage = damage + 1;
            entity.invulnerableTime = 60;
            setTotemDamage(stack, damage);
        }
    }

    public static boolean isNotBroken(ItemStack stack) {
        if (stack == null) return false;
        int damage = getTotemDamage(stack);
        int level = stack.getEnchantmentLevel(Enchantments.UNBREAKING);
        return damage < 3 + level;
    }

    public static int getTotemDamage(ItemStack stack) {
        return !stack.hasTag() ? 0 : stack.getTag().getInt("TotemMDamage");
    }

    public static void setTotemDamage(ItemStack stack, int damage) {
        int level = stack.getEnchantmentLevel(Enchantments.UNBREAKING);
        stack.getOrCreateTag().putInt("TotemMDamage", Mth.clamp(damage, 0, level + 3));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice2", ChatFormatting.GOLD, String.format("%.0f", 100 * raiderBoost.getValue()) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice3", ChatFormatting.GOLD, String.format("%.0f", 100 * raiderResistance.getValue()) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            if (isNotBroken(stack)) {
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
        return isNotBroken(stack);
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING;
    }

    public boolean isBarVisible(ItemStack stack) {
        return getTotemDamage(stack) > 0;
    }

    public int getBarWidth(ItemStack stack) {
        int level = stack.getEnchantmentLevel(Enchantments.UNBREAKING);
        return Math.round(13.0F - (float) getTotemDamage(stack) * 13.0F / (3.0F + level));
    }

    public int getBarColor(ItemStack stack) {
        int level = stack.getEnchantmentLevel(Enchantments.UNBREAKING);
        float stackMaxDamage = 3.0F + level;
        float f = Math.max(0.0F, (stackMaxDamage - getTotemDamage(stack)) / stackMaxDamage);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }
}

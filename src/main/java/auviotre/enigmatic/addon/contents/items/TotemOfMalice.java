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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TotemOfMalice extends ItemBaseCurio implements ICursed, Vanishable {
    public static Omniconfig.DoubleParameter raiderBoost;
    public static Omniconfig.DoubleParameter raiderResistance;
    public static final List<ResourceLocation> extraRaiderList = new ArrayList<>();

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

    public TotemOfMalice() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.RARE).durability(3));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice2", ChatFormatting.GOLD, String.format("%.0f", 100 * raiderBoost.getValue()) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice3", ChatFormatting.GOLD, String.format("%.0f", 100 * raiderResistance.getValue()) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.totemofMalice5");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment != Enchantments.MENDING;
    }

    public static boolean isEnable(Player player) {
        return SuperpositionHandler.isTheCursedOne(player) && (SuperpositionHandler.hasItem(player, EnigmaticAddonItems.TOTEM_OF_MALICE) || SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.TOTEM_OF_MALICE));
    }
}

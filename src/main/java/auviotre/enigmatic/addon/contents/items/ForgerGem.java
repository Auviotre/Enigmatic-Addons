package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ForgerGem extends ItemBaseCurio {
    public static final List<ResourceLocation> blackList = new ArrayList<>();
    private static final String[] defaultBlackList = new String[]{
            "enigmaticaddons:totem_of_malice",
            "twilightforest:glass_sword"
    };
    public static Omniconfig.BooleanParameter strictUnbreakableForge;
    public static Omniconfig.BooleanParameter levelNotValue;
    public static Omniconfig.IntParameter unbreakableRepairCost;

    public ForgerGem() {
        super(getDefaultProperties().rarity(Rarity.EPIC).fireResistant().stacksTo(1));
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("ForgerGem");
        strictUnbreakableForge = builder.comment("Whether to enable stricter detection for unbreakable forging when equipped the Forger's Gem. True for enable.").getBoolean("StrictUnbreakableForge", true);
        levelNotValue = builder.comment("Whether the halving experience by level or value. True for level.").getBoolean("EXPLevelNotValue", false);
        unbreakableRepairCost = builder.comment("The extra repair cost after unbreakable forging when equipped the Forger's Gem.").min(2).getInt("UnbreakableRepairCost", 10);
        blackList.clear();
        String[] list = builder.config.getStringList("ForgerGemUnbreakableBlackList", "Balance Options", defaultBlackList, "List of items that will never be unbreakable. Examples: minecraft:iron_sword. Changing this option required game restart to take effect.");
        Arrays.stream(list).forEach((entry) -> blackList.add(new ResourceLocation(entry)));
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enchantersPearl1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgerGem2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgerGem3");
            if (Minecraft.getInstance().player != null && SuperAddonHandler.isOKOne(Minecraft.getInstance().player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgerGem4");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgerGem5");
            }
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
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
            return super.canEquip(context, stack);
        }
        return false;
    }
}

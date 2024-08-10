package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EarthPromise extends ItemBaseCurio implements ICursed {
    public static Omniconfig.IntParameter cooldown;
    public static Omniconfig.DoubleParameter defaultArmorBonus;
    public static Omniconfig.DoubleParameter defaultToughnessBonus;
    public static Omniconfig.PerhapsParameter breakSpeedBonus;
    public static Omniconfig.PerhapsParameter totalResistance;
    public static Omniconfig.PerhapsParameter abilityTriggerPercent;

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("PromiseoftheEarth");
        breakSpeedBonus = builder.comment("Mining speed boost granted by Promise of the Earth. Defined as percentage.").max(1000).getPerhaps("BreakSpeed", 20);
        defaultArmorBonus = builder.comment("Amount of armor provided by Promise of the Earth.").max(256.0).getDouble("Armor", 5.0);
        defaultToughnessBonus = builder.comment("Amount of armor toughness provided by Promise of the Earth.").max(256.0).getDouble("Toughness", 2.0);
        cooldown = builder.comment("Passive ability cooldown for Promise of the Earth. Measured in ticks. 20 ticks equal to 1 second.").max(32768).getInt("Cooldown", 1000);
        totalResistance = builder.comment("Correction coefficient for the First Curse. Defined as percentage.").max(100).getPerhaps("TotalResistance", 25);
        abilityTriggerPercent = builder.comment("The percentage which trigger the Passive ability, damage / current health. Defined as percentage.").max(100).getPerhaps("AbilityTriggerPercent", 80);
        builder.popPrefix();
    }

    public EarthPromise() {
        super(getDefaultProperties().rarity(Rarity.EPIC));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.earthPromise1", ChatFormatting.GOLD, abilityTriggerPercent.getValue() + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.earthPromise2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.earthPromise3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.earthPromise4", ChatFormatting.GOLD, totalResistance.getValue() + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        }
        ItemLoreHelper.indicateCursedOnesOnly(list);
        this.addAttributes(list, stack);
    }

    @OnlyIn(Dist.CLIENT)
    protected void addAttributes(List<Component> list, ItemStack stack) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.addLocalizedFormattedString(list, "curios.modifiers.ring", ChatFormatting.GOLD);
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.armor", ChatFormatting.GOLD, "+" + defaultArmorBonus.getValue());
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.armor_toughness", ChatFormatting.GOLD, "+" + defaultToughnessBonus.getValue());
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.miningCharm1", ChatFormatting.GOLD, breakSpeedBonus.getValue().asPercentage() + "%");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.miningCharm2", ChatFormatting.GOLD, 2);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        attributes.put(Attributes.ARMOR, new AttributeModifier(UUID.fromString("5b353dce-5f84-c0e8-fa7b-4821a77c3d82"), "Armor bonus", defaultArmorBonus.getValue(), AttributeModifier.Operation.ADDITION));
        attributes.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.fromString("4647d482-8f82-f4c8-5d72-e50a2989fc75"), "Toughness bonus", defaultToughnessBonus.getValue(), AttributeModifier.Operation.ADDITION));
        return attributes;
    }

    public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack curio) {
        LivingEntity entity = slotContext.entity();
        return super.getFortuneLevel(slotContext, lootContext, curio) + 2 + (SuperpositionHandler.hasCurio(entity, EnigmaticItems.MINING_CHARM) ? 1 : 0);
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }


    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (super.canEquip(context, stack)) {
            LivingEntity entity = context.entity();
            return entity instanceof Player player && SuperpositionHandler.isTheCursedOne(player);
        }
        return false;
    }
}

package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuartzRing extends ItemBaseCurio {
    public static Omniconfig.DoubleParameter defaultArmorBonus;
    public static Omniconfig.PerhapsParameter magicResistance;
    public List<ResourceKey<DamageType>> resistanccList = new ArrayList<>();

    public QuartzRing() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.UNCOMMON));
        resistanccList.add(DamageTypes.MAGIC);
        resistanccList.add(DamageTypes.WITHER);
        resistanccList.add(DamageTypes.DRAGON_BREATH);
        resistanccList.add(DamageTypes.INDIRECT_MAGIC);
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("MagicQuartzRing");
        defaultArmorBonus = builder.comment("Default amount of armor provided by Magic Quartz Ring.").max(256.0).getDouble("DefaultArmor", 2);
        magicResistance = builder.comment("Resistance to magic damage provided by Magic Quartz Ring. Defined as percentage.").max(100).getPerhaps("MagicResistance", 30);
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        this.addAttributes(list, stack);
    }

    @OnlyIn(Dist.CLIENT)
    protected void addAttributes(List<Component> list, ItemStack stack) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.addLocalizedFormattedString(list, "curios.modifiers.ring", ChatFormatting.GOLD);
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.magic_resistance", ChatFormatting.GOLD, "+" + magicResistance + "%");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.armor", ChatFormatting.GOLD, "+" + defaultArmorBonus);
        double luck = 1.5;
        if (Minecraft.getInstance().player != null && SuperpositionHandler.hasItem(Minecraft.getInstance().player, EnigmaticAddonItems.ARTIFICIAL_FLOWER))
            luck *= 2;
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.luck", ChatFormatting.GOLD, "+" + luck);
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        double luck = 1.5;
        if (slotContext.entity() instanceof Player player && SuperpositionHandler.hasItem(player, EnigmaticAddonItems.ARTIFICIAL_FLOWER))
            luck *= 2;
        attributes.put(Attributes.ARMOR, new AttributeModifier(UUID.fromString("3b312dce-5f84-c7e5-fa4b-8021a74c3d96"), "Armor bonus", defaultArmorBonus.getValue(), AttributeModifier.Operation.ADDITION));
        attributes.put(Attributes.LUCK, new AttributeModifier(UUID.fromString("233c2c66-ef0c-4036-8101-6540abc9bf47"), "Luck Bonus", luck, AttributeModifier.Operation.ADDITION));
        return attributes;
    }
}

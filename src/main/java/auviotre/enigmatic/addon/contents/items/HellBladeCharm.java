package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.BerserkEmblem;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.UUID;

public class HellBladeCharm extends ItemBaseCurio {
    public static Omniconfig.IntParameter equipCooldown;
    public static Omniconfig.DoubleParameter healMultiplier;
    public static Omniconfig.PerhapsParameter armorDebuff;
    public static Omniconfig.PerhapsParameter killThreshold;
    public static Omniconfig.PerhapsParameter killCursedThreshold;

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("CharmofHellBlade");
        equipCooldown = builder.comment("The Cooldown to unequip this Charm. Defined as tick.").max(32768).getInt("EquipCooldown", 1200);
        armorDebuff = builder.comment("How much less effective armor will be for those who bear the charm. Measured as percentage.").max(100.0).min(50.0).getPerhaps("ArmorDebuff", 100);
        killThreshold = builder.comment("The kill threshold of Hell Blade Charm to active the skill.").max(100).getPerhaps("KillThreshold", 75);
        killCursedThreshold = builder.comment("The kill threshold of Hell Blade Charm to active the skill with Ring of Seven Curses.").max(100).getPerhaps("KillCursedThreshold", 50);
        healMultiplier = builder.comment("The Multiplier of healing when active the skill of Hell Blade Charm.").max(100).getDouble("HealMultiplier", 0.8);
        builder.popPrefix();
    }

    public HellBladeCharm() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC).fireResistant());
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        LocalPlayer player = Minecraft.getInstance().player;
        boolean flag = player != null && SuperpositionHandler.hasCurio(player, EnigmaticItems.BERSERK_CHARM);
        if (Screen.hasShiftDown()) {
            double armorModifier = armorDebuff.getValue().asModifier() * (flag ? 0.6 : 1);
            if (armorModifier == 1.0) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.hellBladeCharm1");
            } else {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.hellBladeCharm1_alt", ChatFormatting.GOLD, String.format("%.0f", armorModifier * 100) + "%");
            }
            if (flag) {
                float boost = (SuperpositionHandler.getMissingHealthPool(player) * 25) + 100;
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.hellBladeCharm2_alt", ChatFormatting.GOLD, String.format("%.0f", boost) + "%");
            } else {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.hellBladeCharm2");
            }
            if (player != null && SuperpositionHandler.isTheCursedOne(player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.hellBladeCharm3", ChatFormatting.GOLD, killCursedThreshold + "%");
            } else {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.hellBladeCharm3", ChatFormatting.GOLD, killThreshold + "%");
            }
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.hellBladeCharm4");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
        this.addAttributes(list, stack, player);
    }

    @OnlyIn(Dist.CLIENT)
    protected void addAttributes(List<Component> list, ItemStack stack, LocalPlayer player) {
        boolean flag = player != null && SuperpositionHandler.hasCurio(player, EnigmaticItems.BERSERK_CHARM);
        float boost = 100.0F + (flag ? (SuperpositionHandler.getMissingHealthPool(player) * (float) BerserkEmblem.attackDamage.getValue() * 20) : 0.0F);
        double armorModifier = armorDebuff.getValue().asModifier() * (flag ? 0.6 : 1);
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.addLocalizedFormattedString(list, "curios.modifiers.charm", ChatFormatting.GOLD);
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.damage", ChatFormatting.GOLD, "+" + String.format("%.0f", boost) + "%");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.armor", ChatFormatting.RED, "-" + String.format("%.0f", armorModifier * 100) + "%");
    }

    public Multimap<Attribute, AttributeModifier> createAttributeMap(Player player) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        double armorModifier = armorDebuff.getValue().asModifier() * (SuperpositionHandler.hasCurio(player, EnigmaticItems.BERSERK_CHARM) ? 0.6 : 1);
        attributes.put(Attributes.ARMOR, new AttributeModifier(UUID.fromString("E7EC5AC7-8D8C-9D83-A87C-1830B55951FA"), "Hell Bonus", -armorModifier, AttributeModifier.Operation.MULTIPLY_TOTAL));
        attributes.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.fromString("03153759-3B92-E47E-EFED-DD4F2ECA6B47"), "Hell Bonus", -armorModifier, AttributeModifier.Operation.MULTIPLY_TOTAL));
        return attributes;
    }


    public void onEquip(SlotContext context, ItemStack prevStack, ItemStack stack) {
        super.onEquip(context, prevStack, stack);
        LivingEntity entity = context.entity();
        if (entity instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            int multiplier = SuperpositionHandler.isTheCursedOne(player) ? 2 : 1;
            player.getCooldowns().addCooldown(this, equipCooldown.getValue() * multiplier);
        }
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            player.getAttributes().addTransientAttributeModifiers(this.createAttributeMap(player));
        }
    }

    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            player.getAttributes().removeAttributeModifiers(this.createAttributeMap(player));
        }
    }

    public boolean canUnequip(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        if (entity instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            return !player.getCooldowns().isOnCooldown(this);
        }
        return super.canUnequip(context, stack);
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }
}

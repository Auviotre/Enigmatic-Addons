package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ISpellstone;
import com.aizistral.enigmaticlegacy.config.EtheriumConfigHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EtheriumCore extends ItemSpellstoneCurio implements ISpellstone {
    public static Omniconfig.IntParameter spellstoneCooldown;
    public static Omniconfig.DoubleParameter armorBonus;
    public static Omniconfig.PerhapsParameter armorMultiplier;
    public static Omniconfig.DoubleParameter armorToughnessBonus;
    public static Omniconfig.PerhapsParameter armorToughnessMultiplier;
    public static Omniconfig.PerhapsParameter knockbackResistance;
    public static Omniconfig.PerhapsParameter damageConversion;
    public static Omniconfig.DoubleParameter damageConversionMax;

    public EtheriumCore() {
        super(ItemSpellstoneCurio.getDefaultProperties().rarity(Rarity.EPIC));
        this.immunityList.add(DamageTypes.CACTUS);
        this.immunityList.add(DamageTypes.CRAMMING);
        this.immunityList.add(DamageTypes.IN_WALL);
        this.immunityList.add(DamageTypes.FALLING_BLOCK);
        this.immunityList.add(DamageTypes.SWEET_BERRY_BUSH);
        this.immunityList.add(DamageTypes.SWEET_BERRY_BUSH);
        this.immunityList.add(DamageTypes.EXPLOSION);
        this.immunityList.add(DamageTypes.PLAYER_EXPLOSION);
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("EtheriumCore");
        spellstoneCooldown = builder.comment("Active ability cooldown for Etherium Core. Measured in ticks. 20 ticks equal to 1 second.").getInt("Cooldown", 1800);
        armorBonus = builder.comment("Default amount of armor points provided by Etherium Core.").max(256.0).getDouble("Armor", 12.0);
        armorMultiplier = builder.comment("The multiplier of armor points of Etherium Core.").max(100).getPerhaps("ArmorMultiplier", 30);
        armorToughnessBonus = builder.comment("The amount of armor toughness provided by Etherium Core when it's bearer has no armor equipped.").max(256.0).getDouble("ArmorToughness", 10.0);
        armorToughnessMultiplier = builder.comment("The multiplier of armor toughness of Etherium Core.").max(100).getPerhaps("ArmorToughnessMultiplier", 50);
        knockbackResistance = builder.comment("Resistance to knockback provided by Etherium Core. Defined as percentage.").max(100.0).getPerhaps("KnockbackResistance", 50);
        damageConversion = builder.comment("The damage amplification conversion ratio of Etherium Core. Defined as percentage.").max(100.0).getPerhaps("DamageConversion", 40);
        damageConversionMax = builder.comment("The max damage amplification of Etherium Core.").max(100.0).getDouble("DamageConversionMax", 25.0);
        builder.popPrefix();
    }

    public static boolean hasShield(Player player) {
        Item item = EnigmaticAddonItems.ETHERIUM_CORE;
        return player != null && SuperpositionHandler.hasCurio(player, item) && (player.getCooldowns().getCooldownPercent(item, 0.0F) > 0.4F || (double) (player.getHealth() / player.getMaxHealth()) <= EtheriumConfigHandler.instance().getShieldThreshold(player).asMultiplier() * 1.5F);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        attributes.put(Attributes.ARMOR, new AttributeModifier(UUID.fromString("554893CF-3922-46D9-A507-FAD7A7669EC0"), "Etherium Armor", armorBonus.getValue(), AttributeModifier.Operation.ADDITION));
        attributes.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.fromString("9B64D316-1C37-44F6-9E2F-239C8B1C0030"), "Etherium Knockback Resistance", knockbackResistance.getValue().asModifier(), AttributeModifier.Operation.ADDITION));
        attributes.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.fromString("DFEE887C-F2C0-491F-BCB6-4148855272CB"), "Etherium Toughness", armorToughnessBonus.getValue(), AttributeModifier.Operation.ADDITION));
        attributes.put(Attributes.ARMOR, new AttributeModifier(UUID.fromString("B5118D05-AB68-49A1-A622-BFAF8340FAB5"), "Etherium Armor Plus", armorMultiplier.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        attributes.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.fromString("A0A3F1D2-D324-4099-8B76-9351478CDD26"), "Etherium Toughness Plus", armorToughnessMultiplier.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        return attributes;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore2", ChatFormatting.GOLD, (float) spellstoneCooldown.getValue() * 0.03F);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCoreCooldown", ChatFormatting.GOLD, (float) spellstoneCooldown.getValue() / 20.0F);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.golemHeart3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore3", ChatFormatting.GOLD, "+" + (int) armorBonus.getValue(), "+" + (int) armorToughnessBonus.getValue());
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore3", ChatFormatting.GOLD, "+" + armorMultiplier.getValue() + "%", "+" + armorToughnessMultiplier.getValue() + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.golemHeart9", ChatFormatting.GOLD, knockbackResistance.getValue().asPercentage() + "%");
            if (ItemNBTHelper.getBoolean(stack, "ArmorInvisibility", false)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore5");
            } else {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore5_alt");
            }
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore6", ChatFormatting.GOLD, damageConversion.getValue().asPercentage() + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore7");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore8");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore9");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        try {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.currentKeybind", ChatFormatting.LIGHT_PURPLE, KeyMapping.createNameSupplier("key.spellstoneAbility").get().getString().toUpperCase());
        } catch (NullPointerException ignored) {
        }
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn) {
        ItemStack stack = player.getItemInHand(handIn);
        if (player.isCrouching()) {
            if (ItemNBTHelper.getBoolean(stack, "ArmorInvisibility", false)) {
                ItemNBTHelper.setBoolean(stack, "ArmorInvisibility", false);
                world.playSound(null, player.blockPosition(), EnigmaticSounds.CHARGED_OFF, SoundSource.PLAYERS, (float) (0.8 + Math.random() * 0.2), (float) (0.8 + Math.random() * 0.2));
            } else {
                ItemNBTHelper.setBoolean(stack, "ArmorInvisibility", true);
                world.playSound(null, player.blockPosition(), EnigmaticSounds.CHARGED_ON, SoundSource.PLAYERS, (float) (0.8 + Math.random() * 0.2), (float) (0.8 + Math.random() * 0.2));
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    public void triggerActiveAbility(Level world, ServerPlayer player, ItemStack stack) {
        if (!SuperpositionHandler.hasSpellstoneCooldown(player)) {
            SuperpositionHandler.setSpellstoneCooldown(player, spellstoneCooldown.getValue());
        }
    }

    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return !context.entity().isCrouching();
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }
}

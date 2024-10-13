package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ISpellstone;
import com.aizistral.enigmaticlegacy.config.EtheriumConfigHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
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
    public static Omniconfig.DoubleParameter defaultArmorBonus;
    public static Omniconfig.DoubleParameter superArmorBonus;
    public static Omniconfig.DoubleParameter superArmorToughnessBonus;
    public static Omniconfig.PerhapsParameter knockbackResistance;
    public static Omniconfig.PerhapsParameter damageConversion;
    public static Omniconfig.DoubleParameter damageConversionMax;
    public Multimap<Attribute, AttributeModifier> attributesDefault = HashMultimap.create();
    public Multimap<Attribute, AttributeModifier> attributesNoArmor = HashMultimap.create();

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("EtheriumCore");
        spellstoneCooldown = builder.comment("Active ability cooldown for Etherium Core. Measured in ticks. 20 ticks equal to 1 second.").getInt("Cooldown", 1800);
        defaultArmorBonus = builder.comment("Default amount of armor points provided by Etherium Core.").max(256.0).getDouble("DefaultArmor", 8.0);
        superArmorBonus = builder.comment("The amount of armor points provided by Etherium Core when it's bearer has no armor equipped.").max(256.0).getDouble("SuperArmor", 30.0);
        superArmorToughnessBonus = builder.comment("The amount of armor toughness provided by Etherium Core when it's bearer has no armor equipped.").max(256.0).getDouble("SuperArmorToughness", 10.0);
        knockbackResistance = builder.comment("Resistance to knockback provided by Etherium Core. Defined as percentage.").max(100.0).getPerhaps("KnockbackResistance", 50);
        damageConversion = builder.comment("The damage amplification conversion ratio of Etherium Core. Defined as percentage.").max(100.0).getPerhaps("DamageConversion", 40);
        damageConversionMax = builder.comment("The max damage amplification of Etherium Core.").max(100.0).getDouble("DamageConversionMax", 25.0);
        builder.popPrefix();
    }

    public EtheriumCore() {
        super(ItemSpellstoneCurio.getDefaultProperties().rarity(Rarity.EPIC));
        this.immunityList.add(DamageTypes.CACTUS);
        this.immunityList.add(DamageTypes.CRAMMING);
        this.immunityList.add(DamageTypes.IN_WALL);
        this.immunityList.add(DamageTypes.FALLING_BLOCK);
        this.immunityList.add(DamageTypes.SWEET_BERRY_BUSH);
        this.initAttributes();
    }

    private void initAttributes() {
        this.attributesDefault.put(Attributes.ARMOR, new AttributeModifier(UUID.fromString("554893CF-3922-46D9-A507-FAD7A7669EC0"), "Etherium DAB", defaultArmorBonus.getValue(), AttributeModifier.Operation.ADDITION));
        this.attributesDefault.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.fromString("9B64D316-1C37-44F6-9E2F-239C8B1C0030"), "Etherium KR", knockbackResistance.getValue().asModifier(), AttributeModifier.Operation.ADDITION));
        this.attributesNoArmor.put(Attributes.ARMOR, new AttributeModifier(UUID.fromString("359E63D8-B593-4E2B-A1EF-CFC6171536FB"), "Etherium SAB", superArmorBonus.getValue(), AttributeModifier.Operation.ADDITION));
        this.attributesNoArmor.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.fromString("DFEE887C-F2C0-491F-BCB6-4148855272CB"), "Etherium STB", superArmorToughnessBonus.getValue(), AttributeModifier.Operation.ADDITION));
        this.attributesNoArmor.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.fromString("0435C95D-C41A-4448-BC5D-E02E9E7F3737"), "Etherium KR", knockbackResistance.getValue().asModifier(), AttributeModifier.Operation.ADDITION));
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
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.golemHeart4", ChatFormatting.GOLD, (int) defaultArmorBonus.getValue());
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.golemHeart5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.golemHeart6", ChatFormatting.GOLD, (int) superArmorBonus.getValue(), (int) superArmorToughnessBonus.getValue());
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.golemHeart9", ChatFormatting.GOLD, knockbackResistance.getValue().asPercentage() + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore4", ChatFormatting.GOLD, damageConversion.getValue().asPercentage() + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.etheriumCore5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.golemHeart10");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        try {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.currentKeybind", ChatFormatting.LIGHT_PURPLE, KeyMapping.createNameSupplier("key.spellstoneAbility").get().getString().toUpperCase());
        } catch (NullPointerException ignored) {
        }
    }

    public void triggerActiveAbility(Level world, ServerPlayer player, ItemStack stack) {
        if (!SuperpositionHandler.hasSpellstoneCooldown(player)) {
            SuperpositionHandler.setSpellstoneCooldown(player, spellstoneCooldown.getValue());
        }
    }

    public static boolean hasShield(Player player) {
        Item item = EnigmaticAddonItems.ETHERIUM_CORE;
        return player != null && SuperpositionHandler.hasCurio(player, item) && (player.getCooldowns().getCooldownPercent(item, 0.0F) > 0.4F || (double) (player.getHealth() / player.getMaxHealth()) <= EtheriumConfigHandler.instance().getShieldThreshold(player).asMultiplier() * 1.5F);
    }

    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity() instanceof ServerPlayer player) {
            AttributeMap map = player.getAttributes();
            map.removeAttributeModifiers(this.attributesDefault);
            map.removeAttributeModifiers(this.attributesNoArmor);
        }
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof ServerPlayer player) {
            AttributeMap map = player.getAttributes();
            if (SuperpositionHandler.hasAnyArmor(player)) {
                map.removeAttributeModifiers(this.attributesDefault);
                map.removeAttributeModifiers(this.attributesNoArmor);
                map.addTransientAttributeModifiers(this.attributesDefault);
            } else {
                map.removeAttributeModifiers(this.attributesDefault);
                map.removeAttributeModifiers(this.attributesNoArmor);
                map.addTransientAttributeModifiers(this.attributesNoArmor);
            }
        }
    }
}

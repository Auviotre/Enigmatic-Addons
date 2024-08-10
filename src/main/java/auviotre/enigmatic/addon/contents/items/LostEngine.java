package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ISpellstone;
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
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class LostEngine extends ItemSpellstoneCurio implements ISpellstone {
    public static Omniconfig.IntParameter spellstoneCooldown;
    public static Omniconfig.PerhapsParameter critModifier;
    public static Omniconfig.DoubleParameter toughnessModifier;
    public static Omniconfig.DoubleParameter KRModifier;
    public static Omniconfig.DoubleParameter speedModifier;
    public static Omniconfig.DoubleParameter gravityModifier;
    public static Omniconfig.DoubleParameter vulnerabilityModifier;

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("LostEngine");
        spellstoneCooldown = builder.comment("Active ability cooldown for Lost Engine. Measured in ticks. 20 ticks equal to 1 second.").getInt("Cooldown", 0);
        critModifier = builder.comment("The crit damage modifier of the Lost Engine.").max(256.0).min(0.0).getPerhaps("CritDamageModifier", 80);
        toughnessModifier = builder.comment("The Armor Toughness modifier of the Lost Engine.").max(256.0).min(0.0).getDouble("ToughnessModifier", 4.0);
        KRModifier = builder.comment("The Knockback resistance modifier of the Lost Engine.").max(256.0).min(0.0).getDouble("KnockbackResistanceModifier", 0.2);
        speedModifier = builder.comment("The speed multiplier of the Lost Engine.").max(1.0).min(0.0).getDouble("SpeedModifier", 0.1);
        gravityModifier = builder.comment("The gravity multiplier of the Lost Engine.").max(1.0).min(0.0).getDouble("GravityModifier", 0.4);
        vulnerabilityModifier = builder.comment("Modifier for Magic Damage vulnerability applied by Lost Engine. Default value of 2.0 means that player will receive twice as much damage from magic.").min(1.0).max(256.0).getDouble("VulnerabilityModifier", 2.5);
    }

    public LostEngine() {
        super(ItemSpellstoneCurio.getDefaultProperties().rarity(Rarity.RARE));
        this.immunityList.add(DamageTypes.FALL);
        this.immunityList.add(DamageTypes.EXPLOSION);
        this.immunityList.add(DamageTypes.PLAYER_EXPLOSION);
        this.immunityList.add(DamageTypes.CACTUS);
        this.immunityList.add(DamageTypes.SWEET_BERRY_BUSH);
        Supplier<Float> magicVulnerabilitySupplier = () -> (float)vulnerabilityModifier.getValue();
        this.resistanceList.put(DamageTypes.MAGIC, magicVulnerabilitySupplier);
        this.resistanceList.put(DamageTypes.WITHER, magicVulnerabilitySupplier);
        this.resistanceList.put(DamageTypes.DRAGON_BREATH, magicVulnerabilitySupplier);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.lostEngine1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.lostEngine2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.lostEngineCooldown", ChatFormatting.GOLD, (float)spellstoneCooldown.getValue() / 20.0F);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.lostEngine3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.lostEngine4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.lostEngine5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.lostEngine6");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.lostEngine7", ChatFormatting.GOLD, critModifier.getValue() + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.lostEngine8");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.lostEngine9");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        try {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.currentKeybind", ChatFormatting.LIGHT_PURPLE, KeyMapping.createNameSupplier("key.spellstoneAbility").get().getString().toUpperCase());
        } catch (NullPointerException ignored) {
        }
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        attributes.put(ForgeMod.ENTITY_GRAVITY.get(), new AttributeModifier(UUID.fromString("299331B9-B0F5-ED8E-EF2F-0DB551E79827"), "Gravity boost", gravityModifier.getValue(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        attributes.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("125BF64C-96A5-5940-57B2-96625C975B7C"), "Speed boost", speedModifier.getValue(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        attributes.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.fromString("EF576F66-2F15-802E-1A98-37F4A8A2D651"), "Armor Toughness boost", toughnessModifier.getValue(), AttributeModifier.Operation.ADDITION));
        attributes.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.fromString("323423C8-E3F4-0AE3-4833-16F26078A927"), "Knockback Resistance boost", KRModifier.getValue(), AttributeModifier.Operation.ADDITION));
        return attributes;
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }
}

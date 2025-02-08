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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.UUID;

public class DespairInsignia extends ItemBaseCurio {
    public static Omniconfig.DoubleParameter attackDamageModifier;
    public static Omniconfig.PerhapsParameter attackSpeedMultiplier;
    public static Omniconfig.PerhapsParameter speedMultiplier;

    public DespairInsignia() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.RARE).fireResistant());
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("InsigniaofDespair");
        attackDamageModifier = builder.comment("The attack damage boost of Insignia of Despair").max(32768).getDouble("AttackDamageModifier", 4);
        attackSpeedMultiplier = builder.comment("The attack speed multiplier of Insignia of Despair. Measures in percentage.").max(100.0).getPerhaps("AttackSpeedMultiplier", 16);
        speedMultiplier = builder.comment("The movement speed multiplier of Insignia of Despair. Measures in percentage.").max(100.0).getPerhaps("MovementSpeedMultiplier", 5);
        builder.popPrefix();
    }

    public CreativeModeTab getCreativeTab() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.adventureCharm1");
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        if (slotContext.entity() instanceof Player) {
            CuriosApi.addSlotModifier(attributes, "charm", UUID.fromString("c1fb4eb8-b251-40be-9f5a-8f9fbe337905"), 1.0, AttributeModifier.Operation.ADDITION);
        }
        attributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("6776b12a-e8b9-4053-a7d1-8d3712443318"), "Attack Damage Bonus", attackDamageModifier.getValue(), AttributeModifier.Operation.ADDITION));
        attributes.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("881fd974-05bf-494c-ba42-c9718e275a01"), "Attack Speed Bonus", attackSpeedMultiplier.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        attributes.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("d3f24c58-05f3-4fa3-81c7-f90ff5ef98bc"), "Speed Bonus", speedMultiplier.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        attributes.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.fromString("28532b8c-20d0-43fc-846c-1c60c074824c"), "Knockback Decrease", -0.05, AttributeModifier.Operation.MULTIPLY_TOTAL));
        return attributes;
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        return super.canEquip(context, stack) && !SuperpositionHandler.hasCurio(entity, EnigmaticAddonItems.ADVENTURE_CHARM);
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return false;
    }

    public void onEquip(SlotContext context, ItemStack prevStack, ItemStack stack) {
        super.onEquip(context, prevStack, stack);
        LivingEntity entity = context.entity();
        if (entity instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            int multiplier = SuperpositionHandler.isTheCursedOne(player) ? 2 : 1;
            player.getCooldowns().addCooldown(this, 600 * multiplier);
        }
    }

    public boolean canUnequip(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        if (entity instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            return !player.getCooldowns().isOnCooldown(this);
        }
        return super.canUnequip(context, stack);
    }
}

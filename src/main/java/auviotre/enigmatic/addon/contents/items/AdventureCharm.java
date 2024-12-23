package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.UUID;

public class AdventureCharm extends ItemBaseCurio {
    public static Omniconfig.BooleanParameter shiftEnable;
    public static Omniconfig.DoubleParameter attackDamageModifier;
    public static Omniconfig.PerhapsParameter attackSpeedMultiplier;

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("EmblemofAdventurer");
        shiftEnable = builder.comment("Whether to enable Shift effect.").getBoolean("ShiftEnable", true);
        attackDamageModifier = builder.comment("The attack damage boost of Emblem of Adventurer").max(32768).getDouble("AttackDamageModifier", 2);
        attackSpeedMultiplier = builder.comment("The attack speed multiplier of Emblem of Adventurer. Measures in percentage.").max(100.0).getPerhaps("AttackSpeedMultiplier", 10);
        builder.popPrefix();
    }

    public AdventureCharm() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.UNCOMMON));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.adventureCharm1");
        if (shiftEnable.getValue())
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.adventureCharm2");
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        if (slotContext.entity() instanceof Player) {
            CuriosApi.addSlotModifier(attributes, "charm", UUID.fromString("4f9d6bf4-49b5-47ed-8796-b0c75e53aa91"), 1.0, AttributeModifier.Operation.ADDITION);
        }
        attributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("6ec66ba8-46e7-487a-8bd1-82eeac5dd4ab"), "Attack Damage Bonus", attackDamageModifier.getValue(), AttributeModifier.Operation.ADDITION));
        attributes.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("131a7a69-ba19-47a7-9ad5-7ce2965a8d6b"), "Attack Speed Bonus", attackSpeedMultiplier.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        return attributes;
    }
}

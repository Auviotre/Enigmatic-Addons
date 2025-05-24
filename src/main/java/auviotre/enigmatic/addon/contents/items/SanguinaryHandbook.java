package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBetrayed;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.BerserkEmblem;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
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

import java.util.List;
import java.util.UUID;

public class SanguinaryHandbook extends ItemBase implements ICursed, IBetrayed {
    public static Omniconfig.DoubleParameter DamageMultiplier;

    public SanguinaryHandbook() {
        super(getDefaultProperties().stacksTo(1).rarity(Rarity.RARE));
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("SanguinaryHuntingHandbook");
        DamageMultiplier = builder.comment("The damage boost on pet of Sanguinary Hunting Handbook.").min(0.0).getDouble("DamageMultiplier", 0.25);
        builder.popPrefix();
    }

    public static Multimap<Attribute, AttributeModifier> createAttributeMap(Player player) {
        Multimap<Attribute, AttributeModifier> attributesDefault = HashMultimap.create();
        float missingHealthPool = SuperpositionHandler.getMissingHealthPool(player);
        attributesDefault.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("f4ece564-d299-40d2-a96a-dc68b493137c"), "enigmaticlegacy:speed_modifier", (double) missingHealthPool * BerserkEmblem.movementSpeed.getValue() * 1.25, AttributeModifier.Operation.MULTIPLY_BASE));
        return attributesDefault;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.sanguinaryHandbook1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.sanguinaryHandbook2", ChatFormatting.GOLD, "+" + DamageMultiplier.getValue() * 100 + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.sanguinaryHandbook3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.sanguinaryHandbook4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.sanguinaryHandbook5");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }
}

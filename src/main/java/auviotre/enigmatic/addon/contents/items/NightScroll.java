package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class NightScroll extends ItemBaseCurio implements ICursed {
    public static Omniconfig.PerhapsParameter abilityBoost;
    public static Omniconfig.IntParameter darkCondition;

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("PactofDarkNight");
        abilityBoost = builder.comment("Default amount of armor points provided by Magic Quartz Ring.").max(100).getPerhaps("AbilityBoost", 10);
        darkCondition = builder.comment("Resistance to magic damage provided by Magic Quartz Ring. Defined as percentage.").max(14).getInt("DarkCondition", 5);
        builder.popPrefix();
    }

    public NightScroll() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.nightScroll1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.nightScroll2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.nightScroll3", ChatFormatting.GOLD, abilityBoost.getValue().asPercentage() + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.nightScroll4", ChatFormatting.GOLD, abilityBoost.getValue().asPercentage() + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.nightScroll5", ChatFormatting.GOLD, abilityBoost.getValue().asPercentage() + "%");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            if (!SuperpositionHandler.isTheCursedOne(player) && !(player.isCreative() || player.isSpectator())) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20));
            }
        }
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player && isDark(player)) {
            attributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("6F27C266-9DCD-22DC-E491-4AF7B6A8CCF9"), "Dark Bonus", NightScroll.abilityBoost.getValue().asModifier(false), AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        return attributes;
    }

    public static boolean isDark(Player player) {
        if (!SuperpositionHandler.isTheCursedOne(player)) return false;
        if (player.hasEffect(MobEffects.DARKNESS)) return true;
        LevelLightEngine lightEngine = player.level().getLightEngine();
        BlockPos blockPos = player.blockPosition();
        return lightEngine.getRawBrightness(blockPos, 7) <= darkCondition.getValue();
    }
}

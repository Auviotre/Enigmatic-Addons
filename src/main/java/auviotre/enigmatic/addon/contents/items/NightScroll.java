package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBetrayed;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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

public class NightScroll extends ItemBaseCurio implements ICursed, IBetrayed {
    public static Omniconfig.PerhapsParameter averageDamageBoost;
    public static Omniconfig.PerhapsParameter averageDamageResistance;
    public static Omniconfig.PerhapsParameter averageLifeSteal;

    public NightScroll() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC));
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("PactofDarkNight");
        averageDamageBoost = builder.comment("The average damage boost modifier provided by Pact of Dark Night.").max(100).getPerhaps("AverageDamageBoost", 20);
        averageDamageResistance = builder.comment("The average damage resistance modifier provided by Pact of Dark Night.").max(100).getPerhaps("AverageDamageResistance", 16);
        averageLifeSteal = builder.comment("The average life stealing modifier provided by Pact of Dark Night.").max(100).getPerhaps("AverageLifeSteal", 8);
        builder.popPrefix();
    }

    public static boolean isDark(Player player) {
        if (!SuperAddonHandler.isOKOne(player)) return false;
        if (player.hasEffect(MobEffects.DARKNESS)) return true;
        LevelLightEngine lightEngine = player.level().getLightEngine();
        BlockPos blockPos = player.blockPosition();
        return true;
    }

    public static float getDarkModifier(Player player) {
        if (player == null || !SuperAddonHandler.isOKOne(player)) return 0.4F;
        if (player.hasEffect(MobEffects.DARKNESS)) return 2.0F;
        LevelLightEngine lightEngine = player.level().getLightEngine();
        BlockPos blockPos = player.blockPosition();
        int i = 16 - lightEngine.getRawBrightness(blockPos, 8);
        return 0.4F + 0.1F * i;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.nightScroll1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.nightScroll2");
            float darkModifier = getDarkModifier(Minecraft.getInstance().player);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.nightScroll3", ChatFormatting.GOLD, (int) (darkModifier * averageDamageBoost.getValue().asPercentage()) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.nightScroll4", ChatFormatting.GOLD, (int) (darkModifier * averageDamageResistance.getValue().asPercentage()) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.nightScroll5", ChatFormatting.GOLD, (int) (darkModifier * averageLifeSteal.getValue().asPercentage()) + "%");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            if (!SuperAddonHandler.isOKOne(player) && !(player.isCreative() || player.isSpectator())) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20));
            }
        }
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player && isDark(player)) {
            attributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("6F27C266-9DCD-22DC-E491-4AF7B6A8CCF9"), "Dark Bonus", averageDamageBoost.getValue().asModifier(false) * getDarkModifier(player), AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        return attributes;
    }
}

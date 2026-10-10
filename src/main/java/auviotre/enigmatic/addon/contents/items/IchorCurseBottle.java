package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBetrayed;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.items.generic.ItemBasePotion;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class IchorCurseBottle extends ItemBasePotion implements ICursed, IBetrayed {
    public static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(EnigmaticAddonEffects.ICHOR_CURSE_EFFECT, 9600 * 4), 1.0F)
            .effect(() -> new MobEffectInstance(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT, 1200), 0.8F)
            .effect(() -> new MobEffectInstance(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT, 1200), 0.4F)
            .effect(() -> new MobEffectInstance(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT, 1200), 0.2F)
            .alwaysEat().build();

    public IchorCurseBottle() {
        super(ItemBase.getDefaultProperties().food(FOOD_PROPERTIES).stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> list, TooltipFlag flag) {
        MutableComponent component = Component.translatable(EnigmaticAddonEffects.ICHOR_CURSE_EFFECT.getDescriptionId());
        list.add(Component.translatable("potion.withDuration", component, Component.literal(StringUtil.formatTickDuration(9600 * 4))).withStyle(ChatFormatting.RED));
        if (SuperAddonHandler.isTheBlessedOne(Minecraft.getInstance().player)) {
            component = Component.translatable(MobEffects.ABSORPTION.getDescriptionId());
            component = Component.translatable("potion.withAmplifier", component, Component.translatable("potion.potency.4"));
            list.add(Component.translatable("potion.withDuration", component, Component.literal(StringUtil.formatTickDuration(2400))).withStyle(ChatFormatting.BLUE));
        }
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public void onConsumed(Level level, Player player, ItemStack stack) {
        player.eat(level, stack);
        if (SuperAddonHandler.isTheBlessedOne(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 4));
        }
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }
}

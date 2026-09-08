package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class SurvivorScroll extends ItemBaseCurio {
    public static Omniconfig.PerhapsParameter threshold;

    public SurvivorScroll() {
        super(ItemBaseCurio.getDefaultProperties());
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("survivalScroll");
        threshold = builder.min(0).max(100).getPerhaps("healthThreshold", 40);
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.suvivalScroll1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.suvivalScroll2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.suvivalScroll3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.suvivalScroll4");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        List<Component> list = super.getAttributesTooltip(tooltips, stack);
        list.add(Component.translatable("attribute.modifier.plus.1", 10, Component.translatable("tooltip.enigmaticaddons.suvivalScrollAttr")).withStyle(ChatFormatting.BLUE));
        return list;
    }

    public static int getBrightness(Level level, BlockPos blockPos) {
        float timeOfDay = level.getTimeOfDay(0.0F);
        boolean hasSkyLight = level.dimensionType().hasSkyLight();
        int skyLight = level.getLightEngine().getRawBrightness(blockPos, 0);
        int blockLight = level.getLightEngine().getRawBrightness(blockPos, 15);
        if (hasSkyLight && (timeOfDay < 0.25 || timeOfDay > 0.75)) {
            double modifier = Math.abs(timeOfDay - 0.5) * 2;
            modifier -= level.isRainingAt(blockPos) ? 0.2 : 0;
            if (blockLight < skyLight) skyLight = Mth.floor(skyLight * Math.sqrt(modifier));
            return Math.max(blockLight, skyLight);
        }
        return blockLight;
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        if (entity.getHealth() < entity.getMaxHealth() * threshold.getValue().asModifier()) {
            entity.getAttributes().addTransientAttributeModifiers(getModifiers());
        } else entity.getAttributes().removeAttributeModifiers(getModifiers());
        if (entity.tickCount % 20 == 0) {
            int brightness = getBrightness(entity.level(), entity.blockPosition());
            if (brightness > 12) {
                entity.heal((brightness - 12) * 0.5F);
            }
        }
    }

    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        context.entity().getAttributes().removeAttributeModifiers(getModifiers());
    }

    protected Multimap<Attribute, AttributeModifier> getModifiers() {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        String s = "Survivor Bonus";
        map.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("19e65114-dad2-4f59-8c7a-574d58204e33"), s, 1, AttributeModifier.Operation.ADDITION));
        map.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.fromString("81a95269-3288-44aa-a639-fc190809d566"), s, 1, AttributeModifier.Operation.ADDITION));
        return map;
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = new ImmutableMultimap.Builder<>();
        String s = "Survivor Bonus";
        builder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(uuid, s, 1.2, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Mod.EventBusSubscriber(modid = EnigmaticAddons.MODID)
    public static class Events {
        @SubscribeEvent(priority = EventPriority.LOW)
        public void onFoodEat(@NotNull LivingEntityUseItemEvent.Start event) {
            LivingEntity entity = event.getEntity();
            if (SuperpositionHandler.hasCurio(entity, EnigmaticAddonItems.SURVIVOR_SCROLL) && entity.getHealth() < entity.getMaxHealth() * threshold.getValue().asModifier()) {
                ItemStack item = event.getItem();
                if (item.getFoodProperties(entity) != null || item.getUseAnimation() == UseAnim.EAT || item.getUseAnimation() == UseAnim.DRINK) {
                    event.setDuration(event.getDuration() * 3 / 4);
                }
            }
        }
    }
}

package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBlessed;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

public class ThunderScroll extends ItemBaseCurio implements ICursed, IBlessed {
    public ThunderScroll() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC));
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static float modify(LivingEntity target, float damage) {
        if (target.getAttributes().hasAttribute(Attributes.ARMOR)) {
            double value = target.getAttribute(Attributes.ARMOR).getValue();
            if (value > 0) {
                double factor = 1.0 - Math.min(0.0375 * value, 0.75);
                damage = (float) (damage / Math.sqrt(factor));
            }
        }
        return damage;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.thunderScroll6");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (super.canEquip(context, stack)) {
            LivingEntity entity = context.entity();
            return entity instanceof Player player && SuperAddonHandler.isOKOne(player);
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLightningHit(LivingDamageEvent event) {
        Entity directEntity = event.getSource().getDirectEntity();
        if (SuperpositionHandler.hasCurio(event.getEntity(), this) && event.getSource().is(DamageTypeTags.IS_LIGHTNING)) {
            if (directEntity != null && directEntity.getTags().contains("HarmlessThunder")) {
                event.setCanceled(true);
                return;
            }
            event.setAmount(event.getAmount() * 0.5F);
            event.getEntity().getPersistentData().putBoolean("ThunderScrollLightningPass", true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onDealt(LivingDamageEvent event) {
        if (SuperpositionHandler.hasCurio(event.getEntity(), this) && event.getSource().is(DamageTypeTags.IS_LIGHTNING)) {
            if (event.getEntity().getPersistentData().getBoolean("ThunderScrollLightningPass")) {
                event.getEntity().getPersistentData().remove("ThunderScrollLightningPass");
                event.setCanceled(false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLightningHit(LivingHurtEvent event) {
        Entity directEntity = event.getSource().getDirectEntity();
        if (SuperpositionHandler.hasCurio(event.getEntity(), this) && event.getSource().is(DamageTypeTags.IS_LIGHTNING)) {
            if (directEntity != null && directEntity.getTags().contains("HarmlessThunder")) {
                event.setCanceled(true);
                return;
            }
            event.getEntity().getPersistentData().putBoolean("ThunderScrollLightningPass", true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onDealt(LivingHurtEvent event) {
        if (SuperpositionHandler.hasCurio(event.getEntity(), this) && event.getSource().is(DamageTypeTags.IS_LIGHTNING)) {
            if (event.getEntity().getPersistentData().getBoolean("ThunderScrollLightningPass")) {
                event.getEntity().getPersistentData().remove("ThunderScrollLightningPass");
                event.setCanceled(false);
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLightningStrike(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof ItemEntity entity && entity.getItem().is(EnigmaticItems.ABYSSAL_HEART)) {
            entity.getPersistentData().putBoolean("Anni", true);
        }
        if (event.getLightning().getTags().contains("HarmlessThunder")) {
            if (event.getEntity() instanceof ItemEntity) {
                event.setCanceled(true);
            } else if (event.getEntity() instanceof LivingEntity entity && SuperpositionHandler.hasCurio(entity, this))
                event.setCanceled(true);
        } else if (event.getEntity() instanceof LivingEntity entity && SuperpositionHandler.hasCurio(entity, this)) {
            entity.getPersistentData().putBoolean("ThunderScrollPass", true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onLightningDealt(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof LivingEntity entity && SuperpositionHandler.hasCurio(entity, this)) {
            if (event.getEntity().getPersistentData().getBoolean("ThunderScrollPass")) {
                event.getEntity().getPersistentData().remove("ThunderScrollPass");
                event.setCanceled(false);
            }
        }
    }
}

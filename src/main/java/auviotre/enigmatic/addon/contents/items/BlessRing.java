package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.triggers.BlessRingEquippedTrigger;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlessRing extends ItemBaseCurio {
    public static final List<String> blessList = new ArrayList<>();
    public static final String CURSED_SPAWN = "CursedNextSpawn";
    public static final String BLESS_SPAWN = "BlessNextSpawn";
    public static Omniconfig.PerhapsParameter damageResistance;
    public static Omniconfig.PerhapsParameter damageBoost;
    public static Omniconfig.IntParameter regenerationSpeed;

    public BlessRing() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC).fireResistant());
        blessList.add("enigmaticlegacy:astral_fruit");
        blessList.add("enigmaticlegacy:twisted_mirror");
        blessList.add("enigmaticlegacy:infernal_shield");
        blessList.add("enigmaticlegacy:berserk_charm");
        blessList.add("enigmaticlegacy:enchanter_pearl");
        blessList.add("enigmaticlegacy:guardian_heart");
        blessList.add("enigmaticlegacy:twisted_heart");
        blessList.add("enigmaticlegacy:curse_transposer");
        blessList.add("enigmaticaddons:night_scroll");
        blessList.add("enigmaticaddons:sanguinary_handbook");
        blessList.add("enigmaticaddons:earth_promise");
        blessList.add("enigmaticaddons:thunder_scroll");
        blessList.add("enigmaticaddons:pure_heart");
        blessList.add("enigmaticaddons:bless_amplifier");
        blessList.add("enigmaticaddons:the_bless");
        blessList.add("enigmaticaddons:scorched_charm");
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("RingofRedemption");
        damageResistance = builder.comment("The damage resistance of the Ring of Redemption. Measured in percentage.").min(0).max(100).getPerhaps("DamageResistance", 40);
        damageBoost = builder.comment("The damage boost of the Ring of Redemption. Measured in percentage.").min(0).max(500).getPerhaps("DamageBoost", 20);
        regenerationSpeed = builder.comment("The time required for each regeneration of Ring of Redemption. Measured in ticks.").min(5).getInt("RegenerationTick", 20);
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing3", ChatFormatting.GOLD, (100 - damageResistance.getValue().asPercentage()) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing4", ChatFormatting.GOLD, damageBoost + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing6", ChatFormatting.GOLD, (CursedRing.lootingBonus.getValue() + 1) / 2);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing7", ChatFormatting.GOLD, (CursedRing.fortuneBonus.getValue() + 1) / 2);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing8");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing9");
        } else {
            if (CursedRing.enableLore.getValue()) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore1");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore2");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore3");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore4");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            }

            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.eternallyBound1");
            if (Minecraft.getInstance().player != null && SuperpositionHandler.canUnequipBoundRelics(Minecraft.getInstance().player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.eternallyBound2_creative");
            } else {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.eternallyBound2");
            }
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }

    public boolean canUnequip(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            if (SuperpositionHandler.canUnequipBoundRelics(player)) {
                return super.canUnequip(context, stack);
            }
        }
        return false;
    }

    public void onEquip(SlotContext context, ItemStack prevStack, ItemStack stack) {
        if (context.entity() instanceof ServerPlayer player) {
            BlessRingEquippedTrigger.INSTANCE.trigger(player);
        }
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        if (entity instanceof Player player && SuperAddonHandler.isPunishedOne(player)) {
            SuperpositionHandler.setPersistentInteger(player, "Punishment", 1 +
                    SuperpositionHandler.getPersistentInteger(player, "Punishment", 0));
            if (SuperpositionHandler.getPersistentInteger(player, "Punishment", 0) % 20 == 0) {
                player.playSound(SoundEvents.TOTEM_USE, 0.6F, 0.0F);
                Level level = player.level();
                if (!level.isClientSide()) {
                    LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(player.level());
                    lightningBolt.moveTo(Vec3.atBottomCenterOf(player.blockPosition()));
                    lightningBolt.setCause(player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
                    player.level().addFreshEntity(lightningBolt);
                }
            }
            if (SuperpositionHandler.getPersistentInteger(player, "Punishment", 0) > 100) {
                SuperpositionHandler.removePersistentTag(player, "Punishment");
                SuperpositionHandler.destroyCurio(player, EnigmaticAddonItems.BLESS_RING);
                SuperpositionHandler.destroyCurio(player, EnigmaticItems.CURSED_RING);
                player.hurt(player.damageSources().fellOutOfWorld(), player.getMaxHealth() * 1000F);
                player.experienceProgress = 0;
                player.experienceLevel = 0;
                player.kill();
            }
        }
        if (entity.tickCount % regenerationSpeed.getValue() == 0 && entity.getHealth() < entity.getMaxHealth() * 0.9F) {
            float delta = entity.getMaxHealth() * 0.9F - entity.getHealth();
            entity.heal(delta / 20.0F);
        }
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (super.canEquip(context, stack)) {
            return context.entity() instanceof Player player && !SuperpositionHandler.isTheCursedOne(player);
        }
        return false;
    }

    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return false;
    }

    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return ICurio.DropRule.ALWAYS_KEEP;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        Map<Enchantment, Integer> list = EnchantmentHelper.getEnchantments(book);
        return !list.containsKey(Enchantments.VANISHING_CURSE) && super.isBookEnchantable(stack, book);
    }

    public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack curio) {
        return super.getFortuneLevel(slotContext, lootContext, curio) + (CursedRing.fortuneBonus.getValue() + 1) / 2;
    }

    public int getLootingLevel(SlotContext slotContext, DamageSource source, LivingEntity target, int baseLooting, ItemStack curio) {
        return super.getLootingLevel(slotContext, source, target, baseLooting, curio) + (CursedRing.lootingBonus.getValue() + 1) / 2;
    }
}

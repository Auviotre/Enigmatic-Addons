package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ExperienceHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.XPScroll;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
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
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CursedXPScroll extends ItemBaseCurio implements ICursed {
    public static Omniconfig.DoubleParameter damageBoostLimit;
    public static Omniconfig.DoubleParameter healBoostLimit;
    public static Omniconfig.DoubleParameter KRBoostLimit;
    public static Omniconfig.IntParameter XPLevelUpperLimit;

    public CursedXPScroll() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.RARE));
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("ScrollofIgnoranceCurse");
        damageBoostLimit = builder.comment("Damage Amplification Limit of Scroll of Ignorance Curse. Defined as percentage.").max(1000.0).getDouble("DamageBoostLimit", 100.0);
        healBoostLimit = builder.comment("Heal Amplification Limit of Scroll of Ignorance Curse. Defined as percentage.").max(1000.0).getDouble("HealBoostLimit", 50.0);
        KRBoostLimit = builder.comment("Knockback Resistance Limit Modifier of Scroll of Ignorance Curse. Defined as percentage.").max(1000.0).getDouble("KnockbackResistanceBoostLimit", 160.0);
        XPLevelUpperLimit = builder.comment("The Max Level of stored experience of Scroll of Ignorance Curse, which provides attribute amplification.").max(1000).getInt("XPLevelUpperLimit", 1000);
        builder.popPrefix();
    }

    public static int getLevel(ItemStack stack) {
        return ExperienceHelper.getLevelForExperience(ItemNBTHelper.getInt(stack, "XPStored", 0));
    }

    public static double getLevelModifier(ItemStack stack) {
        return Mth.clamp((double) getLevel(stack) / CursedXPScroll.XPLevelUpperLimit.getValue(), 0.0D, 1.0D);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        MutableComponent cMode;
        if (!ItemNBTHelper.getBoolean(stack, "IsActive", false)) {
            cMode = Component.translatable("tooltip.enigmaticlegacy.xpTomeDeactivated");
        } else if (ItemNBTHelper.getBoolean(stack, "AbsorptionMode", true)) {
            cMode = Component.translatable("tooltip.enigmaticlegacy.xpTomeAbsorption");
        } else {
            cMode = Component.translatable("tooltip.enigmaticlegacy.xpTomeExtraction");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.xpTome1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.xpTome2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.xpTome3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.xpTome4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.xpTome5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.xpTome10");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.xpTome11", ChatFormatting.GOLD, (int) (XPScroll.xpCollectionRange.getValue() * 1.5));
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.xpTomeMode", null, cMode.getString());
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.xpTomeStoredXP");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.xpTomeUnits", ChatFormatting.GOLD, ItemNBTHelper.getInt(stack, "XPStored", 0), getLevel(stack));

        try {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.currentKeybind", ChatFormatting.LIGHT_PURPLE, KeyMapping.createNameSupplier("key.xpScroll").get().getString().toUpperCase());
        } catch (NullPointerException ignored) {
        }

        if (!Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.indicateCursedOnesOnly(list);
        }
        this.addAttributes(list, stack);
    }

    @OnlyIn(Dist.CLIENT)
    protected void addAttributes(List<Component> list, ItemStack stack) {
        double level = getLevelModifier(stack);
        if (level == 0) return;
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.addLocalizedFormattedString(list, "curios.modifiers.scroll", ChatFormatting.GOLD);
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.damage", ChatFormatting.GOLD, "+" + String.format("%.1f", level * damageBoostLimit.getValue()) + "%");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.heal", ChatFormatting.GOLD, "+" + String.format("%.1f", level * healBoostLimit.getValue()) + "%");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.knockback_resistance", ChatFormatting.GOLD, "+" + String.format("%.1f", level * KRBoostLimit.getValue()) + "%");
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn) {
        ItemStack stack = player.getItemInHand(handIn);
        this.trigger(world, stack, player, handIn, true);
        return InteractionResultHolder.success(stack);
    }

    public void trigger(Level world, ItemStack stack, Player player, InteractionHand hand, boolean swing) {
        if (!player.isCrouching()) {
            if (ItemNBTHelper.getBoolean(stack, "AbsorptionMode", true)) {
                ItemNBTHelper.setBoolean(stack, "AbsorptionMode", false);
                world.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, (float) (0.800000011920929 + Math.random() * 0.20000000298023224));
            } else {
                ItemNBTHelper.setBoolean(stack, "AbsorptionMode", true);
                world.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, (float) (0.800000011920929 + Math.random() * 0.20000000298023224));
            }
        } else if (ItemNBTHelper.getBoolean(stack, "IsActive", false)) {
            ItemNBTHelper.setBoolean(stack, "IsActive", false);
            world.playSound(null, player.blockPosition(), EnigmaticSounds.CHARGED_OFF, SoundSource.PLAYERS, (float) (0.800000011920929 + Math.random() * 0.20000000298023224), (float) (0.800000011920929 + Math.random() * 0.20000000298023224));
        } else {
            ItemNBTHelper.setBoolean(stack, "IsActive", true);
            world.playSound(null, player.blockPosition(), EnigmaticSounds.CHARGED_ON, SoundSource.PLAYERS, (float) (0.800000011920929 + Math.random() * 0.20000000298023224), (float) (0.800000011920929 + Math.random() * 0.20000000298023224));
        }
        if (swing) {
            player.swing(hand);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack stack) {
        return ItemNBTHelper.getBoolean(stack, "IsActive", false);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        double level = getLevelModifier(stack);
        if (entity instanceof Player player && SuperAddonHandler.isOKOne(player) && level > 0) {
            Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
            attributes.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.fromString("1413353D-4D00-953F-B40B-8A17AF92411F"), "Cursed XP Bonus", level / 100.0 * KRBoostLimit.getValue(), AttributeModifier.Operation.MULTIPLY_TOTAL));
            return attributes;
        }
        return super.getAttributeModifiers(slotContext, uuid, stack);
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (super.canEquip(context, stack)) {
            LivingEntity entity = context.entity();
            return entity instanceof Player player && SuperpositionHandler.isTheCursedOne(player);
        }
        return false;
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        ItemStack itemstack = SuperpositionHandler.getCurioStack(context.entity(), EnigmaticAddonItems.CURSED_XP_SCROLL);
        if (context.entity() instanceof Player player && !context.entity().level().isClientSide && ItemNBTHelper.getBoolean(itemstack, "IsActive", false)) {
            Level world = player.level();
            int xpPortion = this.getXPPortion(player);
            int exp;
            if (ItemNBTHelper.getBoolean(itemstack, "AbsorptionMode", true)) {
                if (ExperienceHelper.getPlayerXP(player) >= xpPortion) {
                    ExperienceHelper.drainPlayerXP(player, xpPortion);
                    ItemNBTHelper.setInt(itemstack, "XPStored", ItemNBTHelper.getInt(itemstack, "XPStored", 0) + xpPortion);
                } else if (ExperienceHelper.getPlayerXP(player) > 0 & ExperienceHelper.getPlayerXP(player) < xpPortion) {
                    exp = ExperienceHelper.getPlayerXP(player);
                    ExperienceHelper.drainPlayerXP(player, exp);
                    ItemNBTHelper.setInt(itemstack, "XPStored", ItemNBTHelper.getInt(itemstack, "XPStored", 0) + exp);
                }
            } else {
                exp = ItemNBTHelper.getInt(itemstack, "XPStored", 0);
                if (exp >= xpPortion) {
                    ItemNBTHelper.setInt(itemstack, "XPStored", exp - xpPortion);
                    ExperienceHelper.addPlayerXP(player, xpPortion);
                } else if (exp > 0) {
                    ItemNBTHelper.setInt(itemstack, "XPStored", 0);
                    ExperienceHelper.addPlayerXP(player, exp);
                }
            }

            for (ExperienceOrb experienceOrb : world.getEntitiesOfClass(ExperienceOrb.class, SuperpositionHandler.getBoundingBoxAroundEntity(player, XPScroll.xpCollectionRange.getValue() * 1.5))) {
                if (experienceOrb.isAlive()) {
                    player.takeXpDelay = 0;
                    experienceOrb.playerTouch(player);
                }
            }
        }
    }

    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return false;
    }

    private int getXPPortion(Player player) {
        int level = ExperienceHelper.getPlayerXPLevel(player);
        int levelXP = ExperienceHelper.getExperienceForLevel(level + 1) - ExperienceHelper.getExperienceForLevel(level);
        int portion = levelXP / 5;
        if (level > 100) {
            portion *= 1 + level / 100;
        }

        if (portion > 0) {
            return portion;
        } else {
            Objects.requireNonNull(this);
            return 5;
        }
    }
}

package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.contents.entities.DragonBreathArrow;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICreativeTabMember;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DragonBow extends BowItem implements Vanishable, ICreativeTabMember {
    public static Omniconfig.PerhapsParameter ownerResistance;
    public static Omniconfig.IntParameter maxPotionAmount;

    public DragonBow() {
        super(ItemBase.getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).durability(1024));
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("DragonBreathBow");
        ownerResistance = builder.comment("The Damage Resistance to your own dragon breath damage. Defined as percentage.").max(100).getPerhaps("OwnerResistance", 60);
        maxPotionAmount = builder.comment("The max amount of potion effect you can apply on the arrow.").max(10).min(1).getInt("MaxPotionAmount", 4);
        builder.popPrefix();
    }

    public static float getPowerForTime(int tick) {
        float f = (float) tick / 32.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    public static void resetEffect(ItemStack stack) {
        CompoundTag compoundTag = stack.getOrCreateTag();
        compoundTag.remove("CustomPotionEffects");
    }

    public static void addEffect(ItemStack stack, MobEffectInstance effect) {
        CompoundTag compoundTag = stack.getOrCreateTag();
        List<MobEffectInstance> effects = PotionUtils.getCustomEffects(compoundTag);
        int length = effects.toArray().length;
        if (length < DragonBow.maxPotionAmount.getValue()) {
            effects.add(effect);
        } else {
            List<MobEffectInstance> newEffects = new ArrayList<>();
            for (int i = 1; i < length; i++) newEffects.add(effects.get(i));
            newEffects.add(effect);
            effects = newEffects;
        }
        if (!effects.isEmpty()) {
            ListTag listTag = new ListTag();
            for (MobEffectInstance effectInstance : effects) listTag.add(effectInstance.save(new CompoundTag()));
            compoundTag.put("CustomPotionEffects", listTag);
        }
    }

    public CreativeModeTab getCreativeTab() {
        return EnigmaticTabs.MAIN;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        List<MobEffectInstance> effects = PotionUtils.getCustomEffects(stack);
        if (effects.isEmpty()) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.dragonBow");
        else PotionUtils.addPotionTooltip(stack, list, 0.2F);
        if (stack.isEnchanted()) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
    }

    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int time) {
        if (user instanceof Player player) {
            int i = this.getUseDuration(stack) - time;
            float f = getPowerForTime(i);
            if (f >= 0.1F) {
                stack.hurtAndBreak(1, player, (consumer) -> consumer.broadcastBreakEvent(player.getUsedItemHand()));
                if (!world.isClientSide) {
                    List<MobEffectInstance> effects = PotionUtils.getCustomEffects(stack);
                    DragonBreathArrow arrow;
                    int multi = stack.getEnchantmentLevel(Enchantments.MULTISHOT) > 0 ? 1 : 0;
                    int punch = stack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
                    int power = stack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                    for (int index = -multi; index < 1 + multi; index++) {
                        arrow = new DragonBreathArrow(player, world);
                        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.6F, 1.0F);
                        arrow.setDeltaMovement(arrow.getDeltaMovement().scale(0.95D).add(0.0D, 0.135D * index, 0.0D));
                        if (!effects.isEmpty()) for (MobEffectInstance effect : effects) arrow.addEffect(effect);
                        if (power > 0) arrow.setBaseDamage(arrow.getBaseDamage() + power * 0.8);
                        if (punch > 0) arrow.setKnockback(punch);
                        world.addFreshEntity(arrow);
                    }
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 2.5F / (user.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(itemstack);
    }

    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return (itemStack) -> false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(Items.BOW.getDefaultInstance(), enchantment) && enchantment != Enchantments.INFINITY_ARROWS && enchantment != Enchantments.FLAMING_ARROWS || enchantment == Enchantments.MULTISHOT;
    }
}

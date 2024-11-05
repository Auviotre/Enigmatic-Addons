package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.contents.entities.DragonBreathArrow;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DragonBow extends ItemBase implements Vanishable {
    public static Omniconfig.PerhapsParameter ownerResistance;

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("DragonBreathBow");
        ownerResistance = builder.comment("The Damage Resistance to your own dragon breath damage. Defined as percentage.").max(100).getPerhaps("OwnerResistance", 60);
    }

    public DragonBow() {
        super(ItemBase.getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).durability(1024));
    }

    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int time) {
        if (user instanceof Player player) {
            int i = this.getUseDuration(stack) - time;

            float f = getPowerForTime(i);
            if (f >= 0.1F) {
                if (!world.isClientSide) {
                    DragonBreathArrow dragonBreath = new DragonBreathArrow(player, world);
                    dragonBreath.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.6F, 1.0F);

                    int j = stack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                    if (j > 0) {
                        dragonBreath.setBaseDamage(dragonBreath.getBaseDamage() + j * 0.8);
                    }

                    int k = stack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
                    if (k > 0) {
                        dragonBreath.setKnockback(k);
                    }

                    stack.hurtAndBreak(1, player, (consumer) -> consumer.broadcastBreakEvent(player.getUsedItemHand()));
                    world.addFreshEntity(dragonBreath);
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 2.5F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    public static float getPowerForTime(int tick) {
        float f = (float) tick / 32.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    public int getUseDuration(ItemStack p_40680_) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack p_40678_) {
        return UseAnim.BOW;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(Items.BOW.getDefaultInstance(), enchantment) && enchantment != Enchantments.INFINITY_ARROWS && enchantment != Enchantments.FLAMING_ARROWS;
    }
}

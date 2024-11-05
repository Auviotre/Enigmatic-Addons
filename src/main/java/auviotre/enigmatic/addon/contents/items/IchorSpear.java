package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.contents.entities.ThrownIchorSpear;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class IchorSpear extends ItemBase implements Vanishable {
    public static Omniconfig.IntParameter baseDamage;
    public static Omniconfig.IntParameter duration;
    public static Omniconfig.IntParameter amplifier;
    public static final AbstractProjectileDispenseBehavior DISPENSE_BEHAVIOR = new AbstractProjectileDispenseBehavior() {
        protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
            ThrownIchorSpear spear = new ThrownIchorSpear(level, position.x(), position.y(), position.z());
            spear.pickup = AbstractArrow.Pickup.ALLOWED;
            return spear;
        }

        protected float getPower() {
            return super.getPower() / 1.1F;
        }

        protected void playSound(BlockSource source) {
            source.getLevel().playSound(null, source.getPos(), SoundEvents.TRIDENT_THROW, SoundSource.BLOCKS, 0.8F, 0.6F);
        }
    };

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("IchorSpear");
        baseDamage = builder.comment("The damage of thrown ichor spear").getInt("BaseDamage", 4);
        duration = builder.comment("The duration of the ichor corrosion effect. Measures in ticks.").getInt("EffectDuration", 600);
        amplifier = builder.comment("The amplifier of the ichor corrosion effect.").min(1).getInt("EffectAmplifier", 1);
        builder.popPrefix();
    }

    public IchorSpear() {
        super(ItemBase.getDefaultProperties().rarity(Rarity.RARE));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.ichorSpear");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int tickCount) {
        if (entity instanceof Player player) {
            int duration = this.getUseDuration(stack) - tickCount;
            if (duration >= 5) {
                if (!level.isClientSide) {
                    stack.hurtAndBreak(1, player, (consumer) -> consumer.broadcastBreakEvent(entity.getUsedItemHand()));
                    ThrownIchorSpear spear = new ThrownIchorSpear(player, level);
                    float strength = Math.min(3.5F, duration * 0.1F + 0.6F);
                    float pitch = Math.min(0.65F + duration * 0.02F, 1.1F);
                    spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, strength, 1.0F);

                    level.addFreshEntity(spear);
                    level.playSound(null, spear, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, pitch);
                }

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    if (stack.isEmpty()) player.getInventory().removeItem(stack);
                }

                player.awardStat(Stats.ITEM_USED.get(this));
                player.swing(player.getUsedItemHand());
            }
        }
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemInHand);
    }

    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }
}

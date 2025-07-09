package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.contents.entities.ThrownAstralSpear;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class AstralSpear extends ItemBase implements Vanishable {
    public static Omniconfig.DoubleParameter poweredModifier;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private boolean soundPlayed1 = false;
    private boolean soundPlayed2 = false;
    private boolean soundPlayed3 = false;

    public AstralSpear() {
        super(ItemBase.getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).durability(3000).fireResistant());
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 13.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -3.1F, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("AstralSpear");
        poweredModifier = builder.comment("The damage modifier when spear is powered.").max(100).min(0).getDouble("PoweredModifier", 2.5);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.astralSpear1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.astralSpear2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.astralSpear3");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
        if (stack.isEnchanted()) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
    }

    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int tickCount) {
        if (!level.isClientSide) {
            int duration = this.getUseDuration(stack) - tickCount;
            if (duration >= 60 && !this.soundPlayed3) {
                this.soundPlayed3 = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), EnigmaticSounds.CHARGED_ON, SoundSource.PLAYERS, 2.5F, 1.5F);
            }
            if (duration >= 50 && !this.soundPlayed2) {
                this.soundPlayed2 = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), EnigmaticSounds.CHARGED_ON, SoundSource.PLAYERS, 2.5F, 1.25F);
            }
            if (duration >= 38 && !this.soundPlayed1) {
                this.soundPlayed1 = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), EnigmaticSounds.CHARGED_ON, SoundSource.PLAYERS, 2.5F, 1.0F);
            }
        }
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int tickCount) {
        if (entity instanceof Player player) {
            int duration = this.getUseDuration(stack) - tickCount;
            if (duration >= 12) {
                stack.hurtAndBreak(duration >= 60 ? 5 : 2, player, (consumer) -> consumer.broadcastBreakEvent(entity.getUsedItemHand()));
                if (!level.isClientSide) {
                    ThrownAstralSpear spear = new ThrownAstralSpear(player, level, stack);
                    float strength = Mth.clamp((float) (duration - 8) / 12F, 0.0F, 1.0F);
                    spear.pickup = AbstractArrow.Pickup.DISALLOWED;
                    if (duration >= 60) {
                        spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 4.5F, 1.1F);
                        spear.setPowered(true);
                    } else {
                        spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F + strength, 1.0F);
                    }
                    level.addFreshEntity(spear);
                }
                level.playSound(null, entity, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.5F);
                player.awardStat(Stats.ITEM_USED.get(this));
                player.swing(player.getUsedItemHand());
            }
        }
        this.soundPlayed1 = this.soundPlayed2 = this.soundPlayed3 = false;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(itemInHand);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemInHand);
        }
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity user) {
        stack.hurtAndBreak(2, user, (consumer) -> consumer.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, (p_43276_) -> p_43276_.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getAttributeModifiers(slot, stack);
    }

    public boolean isValidRepairItem(ItemStack stack, ItemStack material) {
        return EnigmaticMaterials.getEtheriumConfig().getRepairMaterial().test(material);
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        boolean base = enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.MENDING;
        boolean curse = base || enchantment == Enchantments.VANISHING_CURSE || enchantment == EnigmaticEnchantments.NEMESIS;
        return enchantment == Enchantments.PIERCING || enchantment == Enchantments.SHARPNESS || curse || super.canApplyAtEnchantingTable(stack, enchantment);
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 20;
    }

    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return super.canPerformAction(stack, toolAction) || ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }

    public @NotNull AABB getSweepHitBox(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity target) {
        return target.getBoundingBox().inflate(2, 0.3, 2);
    }
}

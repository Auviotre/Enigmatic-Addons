package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.contents.entities.ThrownAstralSpear;
import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AstralSpear extends ItemBase implements Vanishable {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private boolean soundPlayed1 = false;
    private boolean soundPlayed2 = false;
    private boolean soundPlayed3 = false;

    public AstralSpear() {
        super(ItemBase.getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).durability(3000));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 13.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -3.1F, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    public CreativeModeTab getCreativeTab() {
        return null;
    }

    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int tickCount) {
        if (!level.isClientSide) {
            int duration = this.getUseDuration(stack) - tickCount;
            if (duration >= 75 && !this.soundPlayed3) {
                this.soundPlayed3 = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), EnigmaticSounds.CHARGED_ON, SoundSource.PLAYERS, 2.5F, 1.7F);
            }
            if (duration >= 65 && !this.soundPlayed2) {
                this.soundPlayed2 = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), EnigmaticSounds.CHARGED_ON, SoundSource.PLAYERS, 2.5F, 1.3F);
            }
            if (duration >= 50 && !this.soundPlayed1) {
                this.soundPlayed1 = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), EnigmaticSounds.CHARGED_ON, SoundSource.PLAYERS, 2.5F, 1.0F);
            }
        }
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int tickCount) {
        if (entity instanceof Player player) {
            int duration = this.getUseDuration(stack) - tickCount;
            if (duration >= 10) {
                stack.hurtAndBreak(duration >= 75 ? 5 : 2, player, (consumer) -> consumer.broadcastBreakEvent(entity.getUsedItemHand()));
                if (!level.isClientSide) {
                    ThrownAstralSpear spear = new ThrownAstralSpear(player, level, stack);
                    float strength = Mth.clamp((float) (duration - 8) / 12F, 0.0F, 1.0F);
                    if (duration >= 75) {
                        spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 4.5F, 1.1F);
                        spear.setPowered(true);
                        level.addFreshEntity(spear);
                        level.playSound(null, spear, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.25F, 2.0F);
                    } else {
                        spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F + strength, 1.0F);
                        level.addFreshEntity(spear);
                        level.playSound(null, spear, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.5F);
                    }

                    if (!player.getAbilities().instabuild) player.getInventory().removeItem(stack);
                }
                this.soundPlayed1 = this.soundPlayed2 = this.soundPlayed3 = false;
                player.awardStat(Stats.ITEM_USED.get(this));
                player.swing(player.getUsedItemHand());
            }
        }
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

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
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
        return enchantment == Enchantments.PIERCING || enchantment == Enchantments.SHARPNESS || enchantment == Enchantments.LOYALTY || curse;
    }

    public int getEnchantmentValue() {
        return 16;
    }
}

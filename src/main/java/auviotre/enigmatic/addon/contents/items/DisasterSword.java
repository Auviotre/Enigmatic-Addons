package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.entities.DisasterChaos;
import auviotre.enigmatic.addon.packets.clients.PacketDisasterParry;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class DisasterSword extends ItemBase implements Vanishable {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    public static Omniconfig.DoubleParameter badOmenBoost;
    public static Omniconfig.DoubleParameter counterattackBoost;
    public static Omniconfig.DoubleParameter critBoost;
    public static Omniconfig.DoubleParameter rangeAttackDamage;

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("DisasterBroadsword");
        badOmenBoost = builder.comment("The damage modifier when you has the Bad Omen effect.").max(100).min(0).getDouble("BadOmenDamageBoost", 0.15);
        counterattackBoost = builder.comment("The damage modifier after you parried with Disaster Broadsword successfully.").max(100).min(0).getDouble("CounterattackBoost", 0.5);
        critBoost = builder.comment("The crit damage modifier after you parried with Disaster Broadsword successfully.").max(100).min(0).getDouble("CritBoost", 1.5);
        rangeAttackDamage = builder.comment("The damage of range attack after you parried the projectiles with Disaster Broadsword successfully.").max(100).min(0).getDouble("RangeAttackDamage", 10);
    }

    public DisasterSword() {
        super(ItemBase.getDefaultProperties().fireResistant().rarity(Rarity.EPIC).durability(2400));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 9.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.3F, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.disasterSword1", ChatFormatting.GOLD, "+" + String.format("%.0f", 100 * badOmenBoost.getValue()) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.disasterSword2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.disasterSword3");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.disasterSword1_alt");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
        if (stack.isEnchanted()) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (player.getPersistentData().getInt("DisasterCounterRange") > 0) {
            if (!level.isClientSide) {
                DisasterChaos chaos = new DisasterChaos(level, player);
                chaos.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.2F, 1.0F);
                level.addFreshEntity(chaos);
                player.getCooldowns().addCooldown(itemInHand.getItem(), 20);
                EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(player.getX(), player.getY(), player.getZ(), 16.0, level.dimension())),
                        new PacketDisasterParry(player.getX(), player.getY(), player.getZ(), 0));
            }
            player.swing(hand);
        } else {
            player.startUsingItem(hand);
            player.getCooldowns().addCooldown(itemInHand.getItem(), 80);
        }
        player.getPersistentData().putInt("DisasterMark", 2);
        return InteractionResultHolder.consume(itemInHand);
    }

    public @NotNull AABB getSweepHitBox(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity target) {
        if (player.getPersistentData().getInt("DisasterCounterattack") > 0) {
            return target.getBoundingBox().inflate(10, 0.3, 10);
        }
        return super.getSweepHitBox(stack, player, target);
    }

    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, (user) -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    public boolean isValidRepairItem(ItemStack self, ItemStack stack) {
        return stack.is(Items.NETHERITE_INGOT) || super.isValidRepairItem(self, stack);
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    public int getUseDuration(ItemStack stack) {
        return 15;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(Items.NETHERITE_SWORD.getDefaultInstance(), enchantment);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }

    public static void parry(Level level, Player blocker, DamageSource source, float damage) {
        if (!level.isClientSide())
            EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(blocker.getX(), blocker.getY(), blocker.getZ(), 64.0, level.dimension())),
                new PacketDisasterParry(blocker.getX(), blocker.getY(), blocker.getZ(), 1));
        Entity directEntity = source.getDirectEntity();
        ItemStack useItem = blocker.getUseItem();
        useItem.hurtAndBreak(Mth.floor(damage / 2), blocker, (user) -> user.broadcastBreakEvent(blocker.getUsedItemHand()));
        blocker.getCooldowns().removeCooldown(useItem.getItem());
        blocker.stopUsingItem();
        blocker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 0, true, false));
        blocker.swing(blocker.getUsedItemHand());
        if (directEntity instanceof LivingEntity living && blocker != living) {
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, blocker.getBoundingBox().inflate(3.2));
            for (LivingEntity entity : entities) {
                if (entity == blocker) continue;
                Vec3 delta = entity.position().subtract(blocker.position()).normalize().scale(0.5);
                float modifier = Math.min(1.0F, 0.8F / entity.distanceTo(blocker));
                Vec3 vec = new Vec3(delta.x, 0, delta.z).normalize().scale(modifier);
                entity.addDeltaMovement(new Vec3(vec.x, entity.onGround() ? 1.2F * modifier : 0.0F, vec.z));
                entity.getPersistentData().putInt("DisasterCurse", 30);
                entity.hurt(entity.damageSources().mobAttack(blocker), 4.0F);
                entity.invulnerableTime = 5;
            }
            blocker.getCooldowns().addCooldown(useItem.getItem(), 24);
            blocker.getPersistentData().putInt("DisasterCounterattack", 50);
        } else if (directEntity instanceof Projectile) {
            blocker.getPersistentData().putInt("DisasterCounterRange", 40);
            blocker.getCooldowns().addCooldown(useItem.getItem(), 8);
        }
    }

    public static void tick(CompoundTag data) {
        int disaster = data.getInt("DisasterCurse");
        if (disaster > 0) {
            data.putInt("DisasterCurse", --disaster);
        } else data.remove("DisasterCurse");
        disaster = data.getInt("DisasterCounterattack");
        if (disaster > 0) {
            data.putInt("DisasterCounterattack", --disaster);
        } else data.remove("DisasterCounterRange");
        disaster = data.getInt("DisasterCounterRange");
        if (disaster > 0) {
            data.putInt("DisasterCounterRange", --disaster);
        } else data.remove("DisasterCounterRange");
    }
}

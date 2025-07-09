package auviotre.enigmatic.addon.contents.entities.goal;


import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;

import java.util.EnumSet;
import java.util.List;

public class CrossbowBlazeAttackGoal<T extends Monster & RangedAttackMob & CrossbowAttackMob> extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final T mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private CrossbowBlazeAttackGoal.CrossbowState crossbowState;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    public CrossbowBlazeAttackGoal(T mob, double modifier, float radius) {
        this.crossbowState = CrossbowBlazeAttackGoal.CrossbowState.UNCHARGED;
        this.mob = mob;
        this.speedModifier = modifier;
        this.attackRadiusSqr = radius * radius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack stack) {
        List<ItemStack> list = Lists.newArrayList();
        CompoundTag compoundtag = stack.getTag();
        if (compoundtag != null && compoundtag.contains("ChargedProjectiles", 9)) {
            ListTag listtag = compoundtag.getList("ChargedProjectiles", 10);
            if (listtag != null) {
                for (int i = 0; i < listtag.size(); ++i) {
                    CompoundTag tag = listtag.getCompound(i);
                    list.add(ItemStack.of(tag));
                }
            }
        }

        return list;
    }

    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow() && SuperAddonHandler.isCurseBoosted(this.mob);
    }

    private boolean isHoldingCrossbow() {
        return this.mob.isHolding((is) -> is.getItem() instanceof CrossbowItem);
    }

    public boolean canContinueToUse() {
        return this.isValidTarget() && (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingCrossbow();
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.setChargingCrossbow(false);
            CrossbowItem.setCharged(this.mob.getUseItem(), false);
        }
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            boolean sighted = this.mob.getSensing().hasLineOfSight(target);
            boolean seen = this.seeTime > 0;
            if (sighted != seen) this.seeTime = 0;
            if (sighted) ++this.seeTime;
            else --this.seeTime;

            double distance = this.mob.distanceToSqr(target);
            boolean flag2 = (distance > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
            if (flag2) {
                --this.updatePathDelay;
                if (this.updatePathDelay <= 0) {
                    this.mob.getNavigation().moveTo(target, this.canRun() ? this.speedModifier : this.speedModifier * 0.5);
                    this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
                }
            } else {
                this.updatePathDelay = 0;
                this.mob.getNavigation().stop();
            }

            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.crossbowState == CrossbowBlazeAttackGoal.CrossbowState.UNCHARGED) {
                if (!flag2) {
                    this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, (item) -> item instanceof CrossbowItem));
                    this.crossbowState = CrossbowBlazeAttackGoal.CrossbowState.CHARGING;
                    this.mob.setChargingCrossbow(true);
                }
            } else if (this.crossbowState == CrossbowBlazeAttackGoal.CrossbowState.CHARGING) {
                if (!this.mob.isUsingItem()) this.crossbowState = CrossbowBlazeAttackGoal.CrossbowState.UNCHARGED;

                if (this.mob.getTicksUsingItem() >= CrossbowItem.getChargeDuration(this.mob.getUseItem())) {
                    this.mob.releaseUsingItem();
                    this.crossbowState = CrossbowBlazeAttackGoal.CrossbowState.CHARGED;
                    this.attackDelay = 5 + this.mob.getRandom().nextInt(15);
                    this.mob.setChargingCrossbow(false);
                }
            } else if (this.crossbowState == CrossbowBlazeAttackGoal.CrossbowState.CHARGED) {
                if (--this.attackDelay == 0) {
                    this.crossbowState = CrossbowBlazeAttackGoal.CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.crossbowState == CrossbowBlazeAttackGoal.CrossbowState.READY_TO_ATTACK && sighted) {
                if (this.mob.getRandom().nextInt(5) == 0) {
                    this.attackDelay = 10 + this.mob.getRandom().nextInt(4);
                    this.crossbowState = CrossbowBlazeAttackGoal.CrossbowState.CHARGED;

                    preformFakeAttack(target);
                } else {
                    ItemStack itemInHand = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, (item) -> item instanceof CrossbowItem));
                    this.crossbowState = CrossbowBlazeAttackGoal.CrossbowState.UNCHARGED;
                    CrossbowItem.setCharged(itemInHand, false);
                    if (this.mob.getRandom().nextInt(6) == 0)
                        this.mob.setItemInHand(InteractionHand.OFF_HAND, getFirework());
                    this.mob.performRangedAttack(target, 1.0F);
                }
            }
        }
    }

    private ItemStack getFirework() {
        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag tag = firework.getOrCreateTagElement("Fireworks");
        ListTag listTag = new ListTag();

        ItemStack star = new ItemStack(Items.FIREWORK_STAR);
        CompoundTag explosion = star.getOrCreateTagElement("Explosion");
        explosion.putByte("Type", (byte) FireworkRocketItem.Shape.BURST.getId());
        explosion.putBoolean("Flicker", true);
        explosion.putBoolean("Trail", true);
        List<Integer> colorList = Lists.newArrayList();
        List<Item> dyes = List.of(Items.BLACK_DYE, Items.WHITE_DYE, Items.GRAY_DYE, Items.LIGHT_GRAY_DYE, Items.BROWN_DYE);
        for (Item dye : dyes) colorList.add(((DyeItem) dye).getDyeColor().getFireworkColor());
        explosion.putIntArray("Colors", colorList);

        CompoundTag starTag = star.getTagElement("Explosion");
        for (int i = 0; i < 5; i++) listTag.add(starTag);
        tag.putByte("Flight", (byte) 1);
        tag.put("Explosions", listTag);
        return firework;
    }

    private void preformFakeAttack(LivingEntity target) {
        InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(this.mob, (item) -> item instanceof CrossbowItem);
        List<ItemStack> chargedProjectiles = getChargedProjectiles(this.mob.getItemInHand(hand));
        if (chargedProjectiles.isEmpty()) return;
        ItemStack itemStack = chargedProjectiles.get(0);
        Projectile projectile;
        if (itemStack.is(Items.FIREWORK_ROCKET)) {
            projectile = new FireworkRocketEntity(this.mob.level(), itemStack, this.mob, this.mob.getX(), this.mob.getEyeY() - 0.15000000596046448, this.mob.getZ(), true);
        } else {
            projectile = new Arrow(this.mob.level(), this.mob);
            ((Arrow) projectile).setEffectsFromItem(itemStack);
            ((Arrow) projectile).setPierceLevel((byte) 1);
        }

        double dx = target.getX() - this.mob.getX();
        double dy = target.getY(0.3333) - projectile.getY();
        double dz = target.getZ() - this.mob.getZ();
        double d3 = Math.sqrt(dx * dx + dz * dz);
        projectile.shoot(dx, dy + d3 * 0.2000, dz, 1.8F, (float) (18 - this.mob.level().getDifficulty().getId() * 4));
        this.mob.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (this.mob.getRandom().nextFloat() * 0.4F + 0.8F));
        this.mob.level().addFreshEntity(projectile);
    }

    private boolean canRun() {
        return this.crossbowState == CrossbowBlazeAttackGoal.CrossbowState.UNCHARGED;
    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}
package auviotre.enigmatic.addon.contents.entities.goal;


import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

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
                if (this.mob.getRandom().nextInt(3) == 0) {
                    this.attackDelay = 8 + this.mob.getRandom().nextInt(8);
                    this.crossbowState = CrossbowBlazeAttackGoal.CrossbowState.CHARGED;

                    preformFakeAttack(target);
                } else {
                    ItemStack itemInHand = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, (item) -> item instanceof CrossbowItem));
                    this.crossbowState = CrossbowBlazeAttackGoal.CrossbowState.UNCHARGED;
                    CrossbowItem.setCharged(itemInHand, false);
                    this.mob.performRangedAttack(target, 1.0F);
                }
            }
        }
    }

    private void preformFakeAttack(LivingEntity target) {
        ItemStack itemStack = this.mob.getProjectile(this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, (item) -> item instanceof CrossbowItem)));
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
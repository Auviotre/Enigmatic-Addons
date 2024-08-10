package auviotre.enigmatic.addon.contents.entities.goal;

import auviotre.enigmatic.addon.contents.entities.CobwebBall;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class SpiderRangedAttackGoal extends Goal {
    private final Monster spider;
    @Nullable
    private LivingEntity target;
    private int attackTime;
    private final double speedModifier;
    private int seeTime;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;
    private boolean hasAmmo;

    public SpiderRangedAttackGoal(Monster spider, double speed, int interval, float range) {
        this(spider, speed, interval, interval, range);
    }

    public SpiderRangedAttackGoal(Monster spider, double speed, int intervalMin, int intervalMax, float range) {
        this.attackTime = -1;
        this.spider = spider;
        this.speedModifier = speed;
        this.attackIntervalMin = intervalMin;
        this.attackIntervalMax = intervalMax;
        this.attackRadius = range;
        this.attackRadiusSqr = range * range;
        this.hasAmmo = true;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        if (!this.hasAmmo) return false;
        LivingEntity mobTarget = this.spider.getTarget();
        if (mobTarget != null && SuperAddonHandler.isCurseBoosted(this.spider) && mobTarget.isAlive()) {
            this.target = mobTarget;
            return true;
        } else {
            return false;
        }
    }

    public boolean canContinueToUse() {
        return (this.canUse() || !this.spider.getNavigation().isDone()) && this.hasAmmo && this.target != null && this.target.isAlive();
    }

    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        double distance = this.spider.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean hasLight = this.spider.getSensing().hasLineOfSight(this.target);
        if (hasLight) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        if (!(distance > (double) this.attackRadiusSqr) && this.seeTime >= 5) {
            this.spider.getNavigation().stop();
        } else {
            this.spider.getNavigation().moveTo(this.target, this.speedModifier);
        }

        this.spider.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (--this.attackTime == 0) {
            if (!hasLight) return;

            float distancePer = (float) Math.sqrt(distance) / this.attackRadius;

            int difficulty = this.spider.level().getDifficulty().getId();
            if (difficulty > 0) {
                CobwebBall ball = new CobwebBall(this.spider.level(), this.spider);
                double dx = this.target.getX() - this.spider.getX();
                double dy = this.target.getY(0.3333333333333333D) - ball.getY();
                double dz = this.target.getZ() - this.spider.getZ();
                double horDist = Math.sqrt(dx * dx + dz * dz);
                ball.shoot(dx, dy + horDist * 0.2D, dz, 1.2F, (float) (14 - difficulty * 3));
                this.spider.playSound(SoundEvents.SHULKER_SHOOT, 1.0F, 1.0F / (this.spider.getRandom().nextFloat() * 0.4F + 0.8F));
                this.spider.level().addFreshEntity(ball);

                if (this.spider.getRandom().nextInt(difficulty) == 0) this.hasAmmo = false;
            } else {
                this.hasAmmo = false;
            }

            this.attackTime = Mth.floor(distancePer * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
        } else if (this.attackTime < 0) {
            this.attackTime = Mth.floor(Mth.lerp(Math.sqrt(distance) / (double) this.attackRadius, this.attackIntervalMin, this.attackIntervalMax));
        }
    }
}

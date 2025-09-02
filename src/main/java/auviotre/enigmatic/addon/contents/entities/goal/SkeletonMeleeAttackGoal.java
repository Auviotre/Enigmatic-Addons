package auviotre.enigmatic.addon.contents.entities.goal;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;

public class SkeletonMeleeAttackGoal extends Goal {
    private final Monster mob;
    private boolean strike = true;

    public SkeletonMeleeAttackGoal(Monster mob) {
        this.mob = mob;
    }

    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !SuperAddonHandler.isCurseBoosted(mob)) return false;
        return this.strike && target.distanceToSqr(this.mob) <= 1.4D && this.mob.getRandom().nextInt(reducedTickDelay(3)) == 0;
    }

    public void start() {
        LivingEntity target = this.mob.getTarget();
        super.start();
        if (target != null && this.strike) {
            double attack = this.mob.getPerceivedTargetDistanceSquareForMeleeAttack(target);
            double reach = this.getAttackReachSqr(target);
            if (attack <= reach) {
                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(target);
                this.mob.push(target);
                target.push(this.mob);
                target.knockback(this.mob.getAttributeValue(Attributes.ATTACK_KNOCKBACK) + 0.5, Mth.sin(this.mob.getYRot() * 0.017453292F), -Mth.cos(this.mob.getYRot() * 0.017453292F));
                this.strike = true;
//                int difficulty = this.mob.level().getDifficulty().getId();
//                if (this.mob.getRandom().nextInt(difficulty + 3) > 0) this.strike = false;
            }
        }
    }

    protected double getAttackReachSqr(LivingEntity target) {
        return this.mob.getBbWidth() * 3.0F + target.getBbWidth();
    }
}

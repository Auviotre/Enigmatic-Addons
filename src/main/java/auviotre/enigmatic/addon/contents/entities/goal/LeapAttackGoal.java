package auviotre.enigmatic.addon.contents.entities.goal;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.phys.Vec3;

public class LeapAttackGoal extends MeleeAttackGoal {
    private final PathfinderMob leaper;
    private final double jumpModifier;
    private int ticksUntilNextJump;

    public LeapAttackGoal(PathfinderMob mob, double jumpModifier) {
        super(mob, 1.0, false);
        this.leaper = mob;
        this.jumpModifier = jumpModifier;
    }

    public boolean canUse() {
        return super.canUse() && leaper.isHolding((item) -> item.is(ItemTags.AXES)) && SuperAddonHandler.isCurseBoosted(this.leaper);
    }

    protected double getAttackReachSqr(LivingEntity entity) {
        if (this.mob.getVehicle() instanceof Ravager) {
            float width = this.mob.getVehicle().getBbWidth() - 0.1F;
            return width * 2.0F * width * 2.0F + entity.getBbWidth();
        } else {
            return super.getAttackReachSqr(entity);
        }
    }

    public void start() {
        super.start();
        this.resetJumpCooldown();
    }

    public void tick() {
        super.tick();
        if (!this.leaper.isPassenger() && this.mob.onGround()) {
            this.ticksUntilNextJump = Math.max(this.ticksUntilNextJump - 1, 0);
            LivingEntity target = this.mob.getTarget();
            if (target != null) {
                double distance = this.mob.distanceToSqr(target);
                if (distance >= 2.25 && distance <= 16.0 && this.getTicksUntilNextJump() <= 0 && this.getTicksUntilNextAttack() <= 3 && this.mob.getRandom().nextInt(reducedTickDelay(2)) == 0) {
                    Vec3 movement = this.mob.getDeltaMovement();
                    Vec3 horizontal = new Vec3(target.getX() - this.mob.getX(), 0.0, target.getZ() - this.mob.getZ());
                    if (horizontal.lengthSqr() > 1.0E-7) {
                        horizontal = horizontal.normalize().scale(0.4).add(movement.scale(0.2));
                    }
                    horizontal.add(target.getDeltaMovement().scale(0.25));
                    this.mob.setDeltaMovement(horizontal.x, jumpModifier, horizontal.z);
                    this.resetJumpCooldown();
                }
            }
        }
    }

    protected int getTicksUntilNextJump() {
        return this.ticksUntilNextJump;
    }

    protected void resetJumpCooldown() {
        this.ticksUntilNextJump = this.adjustedTickDelay(20);
    }
}

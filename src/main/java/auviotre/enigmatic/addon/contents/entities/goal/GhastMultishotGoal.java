package auviotre.enigmatic.addon.contents.entities.goal;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GhastMultishotGoal extends Goal {
    private final Ghast ghast;
    public int chargeTime;

    public GhastMultishotGoal(Ghast ghast) {
        this.ghast = ghast;
    }

    public boolean canUse() {
        return SuperAddonHandler.isCurseBoosted(this.ghast) && this.ghast.getTarget() != null;
    }

    public void start() {
        this.chargeTime = 0;
    }

    public void stop() {
        this.ghast.setCharging(false);
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity target = this.ghast.getTarget();
        if (target != null) {
            if (target.distanceToSqr(this.ghast) < 4096.0 && this.ghast.hasLineOfSight(target)) {
                Level level = this.ghast.level();
                ++this.chargeTime;

                if (this.chargeTime == 20) {
                    Vec3 vector = this.ghast.getViewVector(1.0F);
                    double x = target.getX() - (this.ghast.getX() + vector.x * 4.0);
                    double y = target.getY(0.5) - (0.5 + this.ghast.getY(0.5));
                    double z = target.getZ() - (this.ghast.getZ() + vector.z * 4.0);

                    int difficulty = level.getDifficulty().getId();
                    if (difficulty > 0) {
                        int num = difficulty + ghast.getRandom().nextInt(difficulty) - ghast.getRandom().nextInt(difficulty) / 2;
                        for (int i = 0; i < num; i++) {
                            Vec3 delta = new Vec3(randomV(0.5F), randomV(0.5F), randomV(0.5F));
                            Vec3 F = new Vec3(x, y, z).normalize().scale(0.25F);
                            SmallFireball smallFireball = new SmallFireball(level, this.ghast, x, y, z);
                            smallFireball.setDeltaMovement(new Vec3(F.x * (1 + delta.x * 1.5), F.y * (1 + delta.y * 1.5), F.z * (1 + delta.z * 1.5)));
                            smallFireball.setPos(this.ghast.getX() + vector.x * 5.0 + delta.x, this.ghast.getY(0.5) + 0.5 + delta.y, this.ghast.getZ() + vector.z * 5.0 + delta.z);
                            level.addFreshEntity(smallFireball);
                        }
                    }
                    this.chargeTime = -40;
                }
            } else if (this.chargeTime > 0) {
                --this.chargeTime;
            }
            this.ghast.setCharging(this.chargeTime > 10);
        }
    }

    private float randomV(float range) {
        return (this.ghast.getRandom().nextFloat() - 0.5F) * range * 2.0F;
    }
}

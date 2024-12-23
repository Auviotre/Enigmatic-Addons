package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.contents.items.EvilDagger;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownEvilDagger extends AbstractSpear {
    public int inGroundTick;
    private int curseAmount = 0;

    public ThrownEvilDagger(EntityType<? extends ThrownEvilDagger> type, Level world) {
        super(type, world, EnigmaticAddonItems.EVIL_DAGGER);
        this.setBaseDamage(this.getBaseDamage() / 2);
    }

    public ThrownEvilDagger(LivingEntity entity, Level world) {
        super(EnigmaticAddonEntities.EVIL_DAGGER, entity, world, EnigmaticAddonItems.EVIL_DAGGER);
        this.setBaseDamage(this.getBaseDamage() / 2);
    }

    public ThrownEvilDagger(Level world, double x, double y, double z) {
        super(EnigmaticAddonEntities.EVIL_DAGGER, x, y, z, world, EnigmaticAddonItems.EVIL_DAGGER);
        this.setBaseDamage(this.getBaseDamage() / 2);
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity entity = hitResult.getEntity();
        if (entity == this.getOwner()) return;
        if (entity instanceof LivingEntity living && !living.level().isClientSide) {
            if (this.getOwner() == null || this.getOwner() instanceof LivingEntity owner && owner.canAttack(living)) {
                int percent = (int) (living.getHealth() * 100 / living.getMaxHealth());
                int threshold = living.getPersistentData().getInt("EvilCurseThreshold");
                if (threshold == 0)
                    threshold = percent - EvilDagger.curseDamageRatio.getValue().asPercentage() - this.curseAmount;
                else threshold = threshold - 1 - this.curseAmount / 2;
                living.getPersistentData().putInt("EvilCurseThreshold", Math.max(threshold, 1));
                living.invulnerableTime = 0;
            }
        }
    }

    public void setCurseAmount(int curseAmount) {
        this.curseAmount = curseAmount;
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            Vec3 vec3 = this.getDeltaMovement();
            double dx = vec3.x;
            double dy = vec3.y;
            double dz = vec3.z;
            double length = vec3.length() * 1.2D;
            for (int i = 0; i < length; ++i) {
                this.level().addParticle(ParticleTypes.WITCH, this.getRandomX(0.0F) + dx * (double) i / length, this.getRandomY() + dy * (double) i / length, this.getRandomZ(0.0F) + dz * (double) i / length, -dx * 0.1, -dy * 0.1, -dz * 0.1);
            }
        }
        if (this.inGround) ++this.inGroundTick;
        else this.setDeltaMovement(this.getDeltaMovement().scale(1.1));
        if (this.tickCount > 20 && this.isNoGravity()) this.setNoGravity(false);
        if (this.inGroundTick == 2 || this.tickCount == 36) {
            for (int counter = 0; counter < 16 && this.level().isClientSide; counter++) {
                double theta = Math.random() * 2 * Math.PI;
                double phi = (Math.random() - 0.5D) * Math.PI;
                double dx = Math.cos(theta) * Math.cos(phi) * 0.08D;
                double dy = Math.sin(phi) * 0.08D;
                double dz = Math.sin(theta) * Math.cos(phi) * 0.08D;
                this.level().addParticle(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), dx, dy, dz);
            }
        } else if (this.inGroundTick > 2 || this.tickCount > 36) {
            this.discard();
        }
    }

    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}

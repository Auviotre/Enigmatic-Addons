package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SplitDragonBreath extends AbstractHurtingProjectile {
    public SplitDragonBreath(EntityType<? extends SplitDragonBreath> type, Level world) {
        super(type, world);
    }

    public SplitDragonBreath(Level world, LivingEntity entity) {
        super(EnigmaticAddonEntities.SPLIT_DRAGON_BREATH, entity, 0, 0, 0, world);
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            Vec3 vec3 = this.getDeltaMovement();
            double dx = vec3.x;
            double dy = vec3.y;
            double dz = vec3.z;
            double length = vec3.length() * 1.25D;
            for (int i = 0; i < length; ++i) {
                this.level().addParticle(ParticleTypes.DRAGON_BREATH, this.getRandomX(0.0F) + dx * (double) i / length, this.getRandomY() + dy * (double) i / length, this.getRandomZ(0.0F) + dz * (double) i / length, -dx * 0.1, -dy * 0.1, -dz * 0.1);
            }
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.98F).add(new Vec3(0.0D, -0.05D, 0.0D)));
            }
        }
    }


    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (hitResult.getType() != HitResult.Type.ENTITY || !this.ownedBy(((EntityHitResult) hitResult).getEntity())) {
            if (!this.level().isClientSide) {
                List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0, 2.0, 4.0));
                AreaEffectCloud effectCloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
                if (this.getOwner() instanceof LivingEntity owner) {
                    effectCloud.setOwner(owner);
                }
                effectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
                effectCloud.setRadius(1.0F);
                effectCloud.setDuration(300);
                effectCloud.setWaitTime(0);
                effectCloud.setRadiusPerTick((3.2F - effectCloud.getRadius()) / (float) effectCloud.getDuration());
                effectCloud.addEffect(new MobEffectInstance(EnigmaticAddonEffects.DRAGON_BREATH_EFFECT, 1, 3));
                if (!entities.isEmpty()) {
                    for (LivingEntity entity : entities) {
                        if (this.distanceToSqr(entity) < 9) {
                            effectCloud.setPos(entity.getX(), entity.getY(), entity.getZ());
                            break;
                        }
                    }
                }
                this.level().addFreshEntity(effectCloud);
                this.discard();
            }

        }
    }

    public boolean isPickable() {
        return false;
    }

    public boolean hurt(DamageSource source, float v) {
        return false;
    }

    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.DRAGON_BREATH;
    }

    protected boolean shouldBurn() {
        return false;
    }
}

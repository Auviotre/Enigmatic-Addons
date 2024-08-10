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
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;

public class UltimateDragonFireball extends AbstractHurtingProjectile {
    public UltimateDragonFireball(EntityType<? extends UltimateDragonFireball> type, Level world) {
        super(type, world);
    }

    public UltimateDragonFireball(Level world, LivingEntity owner, double x, double y, double z) {
        super(EnigmaticAddonEntities.ULTIMATE_DRAGON_FIREBALL, owner, x, y, z, world);
    }

    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (hitResult.getType() != HitResult.Type.ENTITY || !this.ownedBy(((EntityHitResult) hitResult).getEntity())) {
            if (!this.level().isClientSide) {
                List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0, 2.0, 4.0));
                AreaEffectCloud effectCloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
                if (this.getOwner() instanceof LivingEntity owner) {
                    effectCloud.setOwner(owner);
                    for (int i = 0; i < 2 * this.level().getDifficulty().getId() + 2; i++) {
                        SplitDragonBreath split = new SplitDragonBreath(this.level(), owner);
                        split.setPos(this.position().add(0.0, -0.05, 0.0));
                        Vec3 movement = new Vec3(Math.random() - 0.5, Math.random() + 0.1, Math.random() - 0.5).normalize();
                        split.setDeltaMovement(movement);
                        this.level().addFreshEntity(split);
                    }
                }

                effectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
                effectCloud.setRadius(2.0F);
                effectCloud.setDuration(640);
                effectCloud.setRadiusPerTick((6.0F - effectCloud.getRadius()) / (float) effectCloud.getDuration());
                effectCloud.addEffect(new MobEffectInstance(EnigmaticAddonEffects.DRAGON_BREATH_EFFECT, 1, 2));
                if (!entities.isEmpty()) {
                    for (LivingEntity entity : entities) {
                        if (this.distanceToSqr(entity) < 16.0) {
                            effectCloud.setPos(entity.getX(), entity.getY(), entity.getZ());
                            break;
                        }
                    }
                }
                this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
                this.level().addFreshEntity(effectCloud);
                boolean flag = ForgeEventFactory.getMobGriefingEvent(this.level(), this.getOwner());
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.8F, flag, Level.ExplosionInteraction.MOB);

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

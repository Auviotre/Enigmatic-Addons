package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class AbyssProjectile extends Projectile {
    private static final EntityDataAccessor<Float> ID_DAMAGE = SynchedEntityData.defineId(AbyssProjectile.class, EntityDataSerializers.FLOAT);
    public int fireTime = 0;
    @Nullable
    private UUID targetUUID;
    @Nullable
    private Entity cachedTarget;

    public AbyssProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public AbyssProjectile(Level world, LivingEntity owner) {
        this(EnigmaticAddonEntities.ABYSS_PROJECTILE, world);
        this.setOwner(owner);
    }

    protected void defineSynchedData() {
        this.entityData.define(ID_DAMAGE, 0.0F);
    }

    public void setDamage(float v) {
        this.entityData.set(ID_DAMAGE, v);
    }

    public void checkTarget() {
        Entity owner = this.getOwner();
        if (owner == null) return;
        List<LivingEntity> livingEntities = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(24), LivingEntity::isAlive);
        for (LivingEntity entity : livingEntities) {
            if (entity instanceof ArmorStand) continue;
            if (entity != null && owner != entity && !entity.isAlliedTo(owner)) {
                this.setTarget(entity);
                break;
            }
        }
    }

    public void tick() {
        super.tick();
        Entity owner = this.getOwner();
        if (this.fireTime > 200 || owner == null) {
            this.discard();
            return;
        }
        if (!level().isClientSide()) {
            Vec3 movement = this.getDeltaMovement();
            Entity cachedTarget = this.getCachedTarget();
            if (cachedTarget != null && cachedTarget.isAlive()) {
                Vec3 target = new Vec3(cachedTarget.getX(), cachedTarget.getY(0.5F), cachedTarget.getZ());
                Vec3 delta = target.subtract(this.position());
                if (delta.length() < 24F) {
                    delta = delta.normalize().scale(0.56).add(movement);
                    movement = delta.scale(0.9F);
                } else this.checkTarget();
                this.setDeltaMovement(movement);
                double d4 = movement.horizontalDistance();
                this.setYRot((float) (Mth.atan2(-movement.x, -movement.z) * 57.2957763671875));
                this.setXRot((float) (Mth.atan2(movement.y, d4) * 57.2957763671875));
            } else this.checkTarget();
            HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitresult))
                this.onHit(hitresult);
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
            this.fireTime++;
        } else {
            Vec3 movement = this.getDeltaMovement();
            for (int i = 1; i < 3; i++) {
                double x = this.getRandomX(0.5) - movement.x * i / 3;
                double y = this.getRandomY() - movement.y * i / 3;
                double z = this.getRandomZ(0.5) - movement.z * i / 3;
                this.level().addParticle(EnigmaticAddonParticles.ABYSS_CHAOS, x, y, z, 0, 0, 0);
                this.level().addParticle(ParticleTypes.WITCH, x, y, z, 0, 0, 0);
            }
        }
    }

    protected boolean canHitEntity(Entity entity) {
        return !this.ownedBy(entity) && super.canHitEntity(entity);
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity livingEntity) {
            Entity target = hitResult.getEntity();
            if (target == this.getOwner()) return;
            int level = target.getPersistentData().getInt("AnnihilationPoint");
            float damage = this.entityData.get(ID_DAMAGE) * 0.25F;
            if (target instanceof LivingEntity living) damage += living.getMaxHealth() * 0.02F * level;
            target.hurt(SuperAddonHandler.damageSource(target, EnigmaticAddonDamageTypes.ABYSS, this, this.getOwner()), damage);
            target.invulnerableTime = 0;
            Vec3 mv = this.getDeltaMovement().scale(0.5);
            ((ServerLevel) level()).sendParticles(ParticleTypes.EXPLOSION, this.getX() + mv.x, this.getY() + mv.y, this.getZ() + mv.z, 1, 0, 0, 0, 0);
            this.discard();
        }
    }

    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(EnigmaticAddonParticles.ABYSS_CHAOS, getX(), getY(), getZ(), 12, 0.5, 0.5, 0.5, 0.02);
            server.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 12, 0.5, 0.5, 0.5, 0.02);
            server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            this.discard();
        }
    }

    public void setTarget(@Nullable Entity entity) {
        if (entity != null) {
            this.targetUUID = entity.getUUID();
            this.cachedTarget = entity;
        }
    }

    @Nullable
    public Entity getCachedTarget() {
        if (this.cachedTarget != null && !this.cachedTarget.isRemoved()) {
            return this.cachedTarget;
        } else if (this.targetUUID != null && this.level() instanceof ServerLevel server) {
            this.cachedTarget = server.getEntity(this.targetUUID);
            return this.cachedTarget;
        } else {
            return null;
        }
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        if (this.targetUUID != null) tag.putUUID("TargetUUID", this.targetUUID);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("TargetUUID")) {
            this.targetUUID = tag.getUUID("TargetUUID");
            this.cachedTarget = null;
        }
    }
}
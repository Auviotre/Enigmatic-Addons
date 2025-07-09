package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class SoulFlameBall extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<Boolean> ID_FIRED = SynchedEntityData.defineId(SoulFlameBall.class, EntityDataSerializers.BOOLEAN);
    public int fireTime = 0;
    @Nullable
    private UUID targetUUID;
    @Nullable
    private Entity cachedTarget;

    public SoulFlameBall(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public SoulFlameBall(Level world, LivingEntity owner) {
        this(EnigmaticAddonEntities.SOUL_FLAME_BALL, world);
        this.setOwner(owner);
        int id = this.getPersistentData().getInt("BallID");
        int offset = id * 18 + owner.tickCount % 90;
        Vec3 position = owner.getPosition(0.0F).add(owner.getDeltaMovement()).add(0.0, owner.getBbHeight() * 0.4, 0.0);
        this.setPos(position.add(Math.sin(Math.PI / 45.0F * offset), 0.0, Math.cos(Math.PI / 45.0F * offset)));
    }

    protected void defineSynchedData() {
        this.entityData.define(ID_FIRED, false);
    }

    public boolean isFired() {
        return this.entityData.get(ID_FIRED);
    }

    public void setFired(boolean isPowered) {
        this.entityData.set(ID_FIRED, isPowered);
    }

    public void checkTarget() {
        Entity owner = this.getOwner();
        if (owner == null || this.tickCount < 10) return;
        List<LivingEntity> livingEntities = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(16.0), entity -> entity.isAlive() && entity instanceof Targeting);
        for (LivingEntity livingEntity : livingEntities) {
            LivingEntity target = ((Targeting) livingEntity).getTarget();
            if (target != null && target.is(owner)) {
                this.setTarget(livingEntity);
                if (!this.isFired())
                    this.setDeltaMovement(this.position().subtract(owner.position()).add(0.0, 0.32, 0.0).scale(1.5));
                this.setFired(true);
                break;
            }
        }
    }

    public void tick() {
        super.tick();
        Entity owner = this.getOwner();
        if (this.fireTime > 125 || owner == null) {
            this.discard();
            return;
        }
        if (!level().isClientSide()) {
            if (this.isFired()) {
                Vec3 movement = this.getDeltaMovement();
                Entity cachedTarget = this.getCachedTarget();
                if (cachedTarget != null) {
                    Vec3 target = new Vec3(cachedTarget.getX(), cachedTarget.getY(0.5F), cachedTarget.getZ());
                    Vec3 delta = target.subtract(this.position());
                    if (delta.length() < 16F) {
                        delta = delta.normalize().scale(0.56).add(movement);
                    } else this.checkTarget();
                    movement = delta.scale(0.7F);
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
                List<Player> playerList = level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(10.0));
                boolean flag = false;
                for (Player player : playerList)
                    if (owner == player) {
                        flag = true;
                        break;
                    }
                if (!flag) this.discard();
                else {
                    this.checkTarget();
                    int id = this.getPersistentData().getInt("BallID");
                    int offset = id * 18 + owner.tickCount % 90;
                    Vec3 position = new Vec3(owner.getX(), owner.getY(), owner.getZ()).add(0.0, owner.getBbHeight() * 0.4, 0.0);
                    this.setPos(position.add(Math.sin(Math.PI / 45.0F * offset), 0.0, Math.cos(Math.PI / 45.0F * offset)));
                }
            }
        } else {
            if (this.random.nextInt(3) == 0)
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(0.5), this.getZ(), 0, 0, 0);
            if (this.random.nextInt(3) == 0)
                this.level().addParticle(ParticleTypes.SOUL, this.getX(), this.getY(0.5), this.getZ(), 0, 0, 0);
            if (this.isFired()) {
                Vec3 movement = this.getDeltaMovement();
                for (int i = 1; i < 3; i++) {
                    double x = this.getX() + movement.x * i / 3;
                    double y = this.getY(0.5) + movement.y * i / 3;
                    double z = this.getZ() + movement.z * i / 3;
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0, 0);
                    this.level().addParticle(ParticleTypes.SOUL, x, y, z, 0, 0, 0);
                }
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
            int level = target.getPersistentData().getInt("IllusionSoulLevel");
            float damage = Math.min(2.0F + level, livingEntity.getMaxHealth() * 0.75F);
            target.hurt(SuperAddonHandler.damageSource(target, DamageTypes.MAGIC, this, this.getOwner()), damage);
            target.invulnerableTime = 0;
            target.getPersistentData().putInt("IllusionSoulLevel", level + 1);
            Vec3 mv = this.getDeltaMovement().scale(0.5);
            ((ServerLevel) level()).sendParticles(ParticleTypes.EXPLOSION, this.getX() + mv.x, this.getY() + mv.y, this.getZ() + mv.z, 1, 0, 0, 0, 0);
            this.discard();
        }
    }

    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!this.level().isClientSide) {
            ((ServerLevel) level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
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

    public ItemStack getItem() {
        return EnigmaticAddonItems.SOUL_FLAME_BALL.getDefaultInstance();
    }
}

package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.client.particles.StarDustParticle;
import auviotre.enigmatic.addon.contents.items.AstralSpear;
import auviotre.enigmatic.addon.packets.clients.PacketStarParticles;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class ThrownAstralSpear extends AbstractSpear {
    private static final EntityDataAccessor<Byte> ID_SHARPNESS = SynchedEntityData.defineId(ThrownAstralSpear.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> ID_TARGET = SynchedEntityData.defineId(ThrownAstralSpear.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ID_POWERED = SynchedEntityData.defineId(ThrownAstralSpear.class, EntityDataSerializers.BOOLEAN);
    private static final double seekDistance = 4.0;
    private static final double seekAngle = Math.PI / 12.0;
    private static final double seekThreshold = 0.9;
    private ItemStack spearItem;
    @Nullable
    private IntOpenHashSet ignoreEntityIds;

    public ThrownAstralSpear(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world, EnigmaticAddonItems.ASTRAL_SPEAR);
        this.spearItem = new ItemStack(EnigmaticAddonItems.ASTRAL_SPEAR);
        this.setBaseDamage(12.0F);
    }

    public ThrownAstralSpear(LivingEntity entity, Level world, ItemStack itemStack) {
        super(EnigmaticAddonEntities.ASTRAL_SPEAR, entity, world, EnigmaticAddonItems.ASTRAL_SPEAR);
        this.spearItem = new ItemStack(EnigmaticAddonItems.ASTRAL_SPEAR);
        this.spearItem = itemStack.copy();
        this.entityData.set(ID_SHARPNESS, (byte) itemStack.getEnchantmentLevel(Enchantments.SHARPNESS));
        this.setPierceLevel((byte) itemStack.getEnchantmentLevel(Enchantments.PIERCING));
        this.setBaseDamage(12.0F);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_SHARPNESS, (byte) 0);
        this.entityData.define(ID_TARGET, -1);
        this.entityData.define(ID_POWERED, false);
    }

    public void tick() {
        if (this.inGroundTime > 3) this.discard();
        Entity owner = this.getOwner();
        if (this.isPowered() && owner != null) {
            if (this.distanceTo(this.getOwner()) > 64) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(1.25));
            }
        }
        if (!this.inGround) {
            if (!this.level().isClientSide) this.updateTarget();
            Entity target = this.getTarget();
            if (target instanceof LivingEntity) {
                Vec3 targetVec = this.getVectorToTarget((LivingEntity) target);
                Vec3 courseVec = this.getDeltaMovement();
                double courseLen = courseVec.length();
                double targetLen = targetVec.length();

                double dotProduct = courseVec.normalize().dot(targetVec.normalize());
                if (dotProduct > seekThreshold) {
                    double factor = (int) getPierceLevel() * 0.03 + 0.25;
                    factor = Mth.clamp(factor, 0.25, 0.36) * (this.isPowered() ? 0.5 : 1.0);
                    double bias = (dotProduct - seekThreshold) / (1 - seekThreshold);
                    Vec3 newMotion = courseVec.scale(1 - factor)
                            .add(targetVec.scale(courseLen / targetLen * factor * Math.sqrt(bias)));
                    this.setDeltaMovement(newMotion.add(0, 0.02F, 0));
                    this.hasImpulse = true;
                } else this.setTarget(null);
            }
        }
        if (!this.inGround && this.level().isClientSide()) {
            Vec3 vec3 = this.getDeltaMovement();
            double dx = vec3.x;
            double dy = vec3.y;
            double dz = vec3.z;
            double length = vec3.length() * 1.44D;
            for (int i = 0; i < length + 1; ++i) {
                this.level().addParticle(StarDustParticle.get(this.random), this.getRandomX(0.1F) + dx * (double) i / length, this.getRandomY() + dy * (double) i / length, this.getRandomZ(0.1F) + dz * (double) i / length, -dx * 0.1, -dy * 0.1, -dz * 0.1);
                if (this.inGround) break;
            }
        }
        super.tick();
    }

    private void resetPiercedEntities() {
        if (this.ignoreEntityIds != null) this.ignoreEntityIds.clear();
    }


    protected void onHitEntity(EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity();
        Entity owner = this.getOwner();
        double damage = this.getBaseDamage();
        if (entity instanceof LivingEntity livingEntity) {
            damage += EnchantmentHelper.getDamageBonus(this.spearItem, livingEntity.getMobType());
        }
        if (this.getPierceLevel() > 0 && !this.isPowered()) {
            if (this.ignoreEntityIds == null) this.ignoreEntityIds = new IntOpenHashSet(5);
            if (this.ignoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }
            this.ignoreEntityIds.add(entity.getId());
        }
        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);
        if (entity.hurt(damageSource, (float) damage)) {
            if (entity.getType() == EntityType.ENDERMAN) return;
            if (entity instanceof LivingEntity living) {
                if (owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(living, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, living);
                }
                this.doPostHurtEffects(living);
            }
            if (this.getPierceLevel() <= 0) this.discard();
        }
        if (this.isPowered()) this.areaAttack(this.level(), hitResult, damageSource, (float) damage);
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    protected void onHitBlock(BlockHitResult hitResult) {
        if (this.isPowered()) {
            Entity owner = this.getOwner();
            double damage = this.getBaseDamage() + (this.entityData.get(ID_SHARPNESS) > 0 ? this.entityData.get(ID_SHARPNESS) : 0);
            DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);
            areaAttack(this.level(), hitResult, damageSource, (float) damage);
        }
        super.onHitBlock(hitResult);
        this.resetPiercedEntities();
    }

    protected void areaAttack(Level level, HitResult result, DamageSource source, float damage) {
        damage = damage * (float) AstralSpear.poweredModifier.getValue();
        Vec3 location = result.getLocation();
        if (result instanceof EntityHitResult hitResult)
            location.add(0, hitResult.getEntity().getBbHeight() * 0.75, 0);
        if (!level.isClientSide)
            EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(location.x, location.y, location.z, 64.0, level.dimension())),
                    new PacketStarParticles(location.x, location.y, location.z, 160, 0));
        Entity owner = this.getOwner() == null ? this : this.getOwner();
        List<Entity> entities = level.getEntities(this, this.getBoundingBox().move(location.subtract(this.position())).inflate(5));
        for (Entity target : entities) {
            if (target instanceof LivingEntity && target.isAlive() && target != owner && target != this) {
                target.hurt(source, damage);
            }
        }
        this.playSound(SoundEvents.TRIDENT_HIT_GROUND, 2.0F, 0.0F);
        this.resetPiercedEntities();
        this.setPowered(false);
    }

    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && (this.ignoreEntityIds == null || !this.ignoreEntityIds.contains(entity.getId()));
    }

    private boolean isAcceptableReturnOwner() {
        Entity owner = this.getOwner();
        if (owner != null && owner.isAlive()) {
            return !(owner instanceof ServerPlayer) || !owner.isSpectator();
        } else {
            return false;
        }
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AstralSpear", 10)) {
            this.spearItem = ItemStack.of(tag.getCompound("AstralSpear"));
        }
        this.entityData.set(ID_SHARPNESS, (byte) this.spearItem.getEnchantmentLevel(Enchantments.SHARPNESS));
        this.setPowered(tag.getBoolean("IsPowered"));
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("AstralSpear", this.spearItem.save(new CompoundTag()));
        tag.putBoolean("IsPowered", this.isPowered());
    }

    public ItemStack getItem() {
        return this.spearItem.copy();
    }

    protected ItemStack getPickupItem() {
        return this.spearItem.copy();
    }

    private void updateTarget() {
        Entity target = this.getTarget();
        if (target != null && !target.isAlive()) {
            target = null;
            this.setTarget(null);
        }

        if (target == null) {
            AABB positionBB = new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ());
            AABB targetBB = positionBB;

            Vec3 courseVec = this.getDeltaMovement().scale(seekDistance).yRot((float) seekAngle);
            targetBB = targetBB.minmax(positionBB.move(courseVec));

            courseVec = this.getDeltaMovement().scale(seekDistance).yRot((float) -seekAngle);
            targetBB = targetBB.minmax(positionBB.move(courseVec));

            targetBB = targetBB.inflate(0, seekDistance * 0.5, 0);

            double closestDot = -1.0;
            LivingEntity closestTarget = null;

            List<LivingEntity> entityList = this.level().getEntitiesOfClass(LivingEntity.class, targetBB);
            List<LivingEntity> monsters = entityList.stream().filter(living -> living instanceof Monster).toList();

            if (!monsters.isEmpty()) {
                for (LivingEntity living : monsters) {
                    if (!living.hasLineOfSight(this)) continue;
                    if (living == this.getOwner()) continue;
                    if (!this.canHitEntity(living))
                        continue;
                    Vec3 motionVec = this.getDeltaMovement().normalize();
                    Vec3 targetVec = this.getVectorToTarget(living).normalize();

                    double dot = motionVec.dot(targetVec);
                    if (dot > Math.max(closestDot, seekThreshold)) {
                        closestDot = dot;
                        closestTarget = living;
                    }
                }
            } else {
                for (LivingEntity living : entityList) {
                    if (!living.hasLineOfSight(this)) continue;
                    if (living == this.getOwner()) continue;
                    if (this.getOwner() != null && living instanceof TamableAnimal animal && animal.getOwner() == this.getOwner())
                        continue;
                    if (!this.canHitEntity(living))
                        continue;
                    Vec3 motionVec = this.getDeltaMovement().normalize();
                    Vec3 targetVec = this.getVectorToTarget(living).normalize();

                    double dot = motionVec.dot(targetVec);
                    if (dot > Math.max(closestDot, seekThreshold)) {
                        closestDot = dot;
                        closestTarget = living;
                    }
                }
            }

            if (closestTarget != null) {
                setTarget(closestTarget);
            }
        }
    }

    private Vec3 getVectorToTarget(LivingEntity target) {
        return new Vec3(target.getX() - this.getX(), target.getY(0.81F) - this.getY(), target.getZ() - this.getZ());
    }

    @Nullable
    private Entity getTarget() {
        return this.level().getEntity(this.getEntityData().get(ID_TARGET));
    }

    private void setTarget(@Nullable Entity entity) {
        this.getEntityData().set(ID_TARGET, entity == null ? -1 : entity.getId());
    }

    public boolean isPowered() {
        return this.entityData.get(ID_POWERED);
    }

    public void setPowered(boolean isPowered) {
        this.entityData.set(ID_POWERED, isPowered);
    }
}

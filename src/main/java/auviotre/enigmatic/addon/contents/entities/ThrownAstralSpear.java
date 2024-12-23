package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.client.particles.StarDustParticle;
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
import net.minecraft.world.entity.*;
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
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownAstralSpear.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> ID_TARGET = SynchedEntityData.defineId(ThrownAstralSpear.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ID_POWERED = SynchedEntityData.defineId(ThrownAstralSpear.class, EntityDataSerializers.BOOLEAN);
    private static final double seekDistance = 4.0;
    private static final double seekAngle = Math.PI / 15.0;
    private static final double seekThreshold = 0.5;
    private boolean dealtDamage;
    private ItemStack spearItem;
    public int clientReturnTickCount;
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
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(itemStack));
        this.setPierceLevel((byte) itemStack.getEnchantmentLevel(Enchantments.PIERCING));
        this.setBaseDamage(12.0F);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_SHARPNESS, (byte) 0);
        this.entityData.define(ID_LOYALTY, (byte) 0);
        this.entityData.define(ID_TARGET, -1);
        this.entityData.define(ID_POWERED, false);
    }

    public void tick() {
        if (this.inGroundTime > 3) this.dealtDamage = true;
        Entity owner = this.getOwner();
        if (this.isPowered() && !this.dealtDamage && owner != null) {
            if (this.distanceTo(this.getOwner()) > 64) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(1.25));
            }
        }
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (loyalty > 0 && (this.dealtDamage || this.isNoPhysics()) && owner != null) {
            if (!this.isAcceptableReturnOwner()) {
                if (!this.level().isClientSide && this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 pos = owner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + pos.y * 0.015 * (double) loyalty, this.getZ());
                if (this.level().isClientSide) this.yOld = this.getY();

                double scale = 0.07 * (double) loyalty;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(pos.normalize().scale(scale)));
                if (this.clientReturnTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.5F);
                }
                ++this.clientReturnTickCount;
            }
        }
        if (!this.dealtDamage && !this.inGround) {
            this.updateTarget();
            Entity target = this.getTarget();
            if (target != null) {
                Vec3 targetVec = this.getVectorToTarget(target);
                Vec3 courseVec = this.getDeltaMovement();

                double courseLen = courseVec.length();
                double targetLen = targetVec.length();

                double dotProduct = courseVec.dot(targetVec) / (courseLen * targetLen);
                if (dotProduct > seekThreshold) {
                    double factor = (int) getPierceLevel() * 0.03 + 0.2;
                    factor = Mth.clamp(factor, 0.2, 0.35) * (this.isPowered() ? 0.5 : 1.0);
                    Vec3 newMotion = courseVec.scale(1 - factor).add(targetVec.scale(courseLen / targetLen * factor));
                    this.setDeltaMovement(newMotion.add(0, 0.01F, 0));
                    this.hasImpulse = true;
                } else this.setTarget(null);
            }
        }
        if (!this.inGround) {
            Vec3 vec3 = this.getDeltaMovement();
            double dx = vec3.x;
            double dy = vec3.y;
            double dz = vec3.z;
            double length = vec3.length() * 1.6D;
            for (int i = 0; i < length; ++i) {
                this.level().addParticle(StarDustParticle.get(this.random), this.getRandomX(0.1F) + dx * (double) i / length, this.getRandomY() + dy * (double) i / length, this.getRandomZ(0.1F) + dz * (double) i / length, -dx * 0.1, -dy * 0.1, -dz * 0.1);
                if (this.inGround) break;
            }
        }
        super.tick();
    }

    @Nullable
    protected EntityHitResult findHitEntity(Vec3 vec1, Vec3 vec2) {
        return this.dealtDamage ? null : super.findHitEntity(vec1, vec2);
    }

    private void resetPiercedEntities() {
        if (this.ignoreEntityIds != null) this.ignoreEntityIds.clear();
    }

    private void stop() {
        this.dealtDamage = true;
        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.02, -0.1, -0.02));
        if (this.isPowered()) this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
        this.setPowered(false);
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity();
        Entity owner = this.getOwner();
        double damage = this.getBaseDamage() + (this.entityData.get(ID_SHARPNESS) > 0 ? this.entityData.get(ID_SHARPNESS) : 0);
        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);
        if (this.isPowered()) {
            this.areaAttack(this.level(), hitResult, damageSource, (float) (damage * 2));
            this.stop();
        } else {
            if (this.getPierceLevel() > 0 && entity.isAlive()) {
                if (this.ignoreEntityIds == null) this.ignoreEntityIds = new IntOpenHashSet(9);
                Vec3 movement = this.getDeltaMovement();
                AABB box = this.getBoundingBox().move(movement).inflate(movement.length() / 1.5);
                boolean flag = true;
                for (int i = 0; i < 5 && flag; i++) {
                    List<LivingEntity> entityList = this.level().getEntitiesOfClass(LivingEntity.class, box);
                    box = box.move(movement.add(0.0, -0.1, 0.0));
                    for (LivingEntity living : entityList) {
                        if (living.is(entity) || !this.canHitEntity(living)) continue;
                        if (living == this.getOwner()) continue;
                        flag = false;
                    }
                }
                if (this.ignoreEntityIds.size() >= this.getPierceLevel() || flag) this.stop();
                this.ignoreEntityIds.add(entity.getId());
            } else this.stop();
            if (entity.hurt(damageSource, (float) damage)) {
                if (entity.getType() == EntityType.ENDERMAN) return;
                if (entity instanceof LivingEntity living) {
                    if (owner instanceof LivingEntity) {
                        EnchantmentHelper.doPostHurtEffects(living, owner);
                        EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, living);
                    }
                    this.doPostHurtEffects(living);
                }
            }
            this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
        }
    }

    protected void onHitBlock(BlockHitResult hitResult) {
        if (this.isPowered() && !this.dealtDamage) {
            Entity owner = this.getOwner();
            double damage = this.getBaseDamage() + (this.entityData.get(ID_SHARPNESS) > 0 ? this.entityData.get(ID_SHARPNESS) : 0);
            DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);
            areaAttack(this.level(), hitResult, damageSource, (float) damage * 2);
            this.dealtDamage = true;
        }
        super.onHitBlock(hitResult);
        this.resetPiercedEntities();
    }

    protected void areaAttack(Level level, HitResult result, DamageSource source, float damage) {
        Vec3 location = result.getLocation();
        if (result instanceof EntityHitResult hitResult)
            location.add(0, hitResult.getEntity().getBbHeight() * 0.75, 0);
        EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(location.x, location.y, location.z, 64.0, level.dimension())),
                new PacketStarParticles(location.x, location.y, location.z, 160, 0));
        Entity owner = this.getOwner() == null ? this : this.getOwner();
        List<Entity> entities = level.getEntities(this, this.getBoundingBox().move(location.subtract(this.position())).inflate(5));
        for (Entity target : entities) {
            if (target instanceof Mob && target.isAlive() && target != owner && target != this) {
                target.hurt(source, damage);
            }
        }
        this.playSound(SoundEvents.TRIDENT_HIT_GROUND, 2.0F, 0.0F);

        this.resetPiercedEntities();
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
        this.dealtDamage = tag.getBoolean("DealtDamage");
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(this.spearItem));
        this.entityData.set(ID_SHARPNESS, (byte) this.spearItem.getEnchantmentLevel(Enchantments.SHARPNESS));
        this.setPowered(tag.getBoolean("IsPowered"));
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("AstralSpear", this.spearItem.save(new CompoundTag()));
        tag.putBoolean("DealtDamage", this.dealtDamage);
        tag.putBoolean("IsPowered", this.isPowered());
    }

    public ItemStack getItem() {
        return this.spearItem.copy();
    }

    protected ItemStack getPickupItem() {
        return this.spearItem.copy();
    }

    public void tickDespawn() {
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (this.pickup != Pickup.ALLOWED || loyalty <= 0) {
            super.tickDespawn();
        }
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
            Entity closestTarget = null;

            List<LivingEntity> entityList = this.level().getEntitiesOfClass(LivingEntity.class, targetBB);
            List<LivingEntity> monsters = entityList.stream().filter(living -> living instanceof Monster).toList();

            if (!monsters.isEmpty()) {
                for (LivingEntity monster : monsters) {
                    if (((Monster) monster).getTarget() == this.getOwner() && this.canHitEntity(monster)) {
                        this.setTarget(monster);
                        return;
                    }
                }
                for (LivingEntity monster : monsters) {
                    if (monster instanceof NeutralMob) continue;
                    if (monster.hasLineOfSight(this) && this.canHitEntity(monster)) {
                        this.setTarget(monster);
                        return;
                    }
                }
            }

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

            if (closestTarget != null) {
                setTarget(closestTarget);
            }
        }
    }

    private Vec3 getVectorToTarget(Entity target) {
        return new Vec3(target.getX() - this.getX(), target.getY(0.81F) - this.getY(), target.getZ() - this.getZ());
    }

    @Nullable
    private Entity getTarget() {
        return this.level().getEntity(this.getEntityData().get(ID_TARGET));
    }

    private void setTarget(@Nullable Entity entity) {
        this.getEntityData().set(ID_TARGET, entity == null ? -1 : entity.getId());
    }

    public void setPowered(boolean isPowered) {
        this.entityData.set(ID_POWERED, isPowered);
    }

    public boolean isPowered() {
        return this.entityData.get(ID_POWERED);
    }
}

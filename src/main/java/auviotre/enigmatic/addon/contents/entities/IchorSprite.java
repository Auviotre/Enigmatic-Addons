package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import auviotre.enigmatic.addon.registries.EnigmaticAddonMemories;
import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class IchorSprite extends PathfinderMob implements TraceableEntity {
    public static final int BEAM_RANGE = 8;
    protected static final ImmutableList<SensorType<? extends Sensor<? super IchorSprite>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.HURT_BY
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            EnigmaticAddonMemories.ICHOR_SPRITE_OWNER,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.PATH,
            MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
            MemoryModuleType.ANGRY_AT
    );
    private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData.defineId(IchorSprite.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TIMER = SynchedEntityData.defineId(IchorSprite.class, EntityDataSerializers.INT);
    @Nullable
    private LivingEntity clientSideCachedAttackTarget;
    private int clientAnimationTime;
    private int clientAnimationTime0;

    public IchorSprite(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.MOVEMENT_SPEED, 0.16F)
                .add(Attributes.FLYING_SPEED, 0.15F).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.FOLLOW_RANGE, 48.0);
    }

    public double getMyRidingOffset() {
        return 0.04;
    }

    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.6F;
    }

    private static float getOffset(RandomSource random, float range) {
        return (random.nextFloat() * 2 - 1.0F) * range;
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
        this.entityData.define(DATA_ATTACK_TIMER, 0);
    }

    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    protected Brain.Provider<IchorSprite> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return Ai.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    public Brain<IchorSprite> getBrain() {
        return (Brain<IchorSprite>) super.getBrain();
    }

    public void aiStep() {
        if (this.isAlive()) {
            int attackTimer = this.getAttackTimer();
            if (this.level().isClientSide) {
                this.clientAnimationTime0 = this.clientAnimationTime;
                if (this.hasActiveAttackTarget()) {
                    if (this.clientAnimationTime < 30) this.clientAnimationTime += this.random.nextInt(3, 6);
                    LivingEntity target = this.getActiveAttackTarget();
                    if (target != null) {
                        this.getLookControl().setLookAt(target, 90.0F, 90.0F);
                        this.getLookControl().tick();
                    }
                } else if (this.getBrain().isActive(Activity.IDLE) && !this.getMainHandItem().isEmpty()) {
                    if (this.clientAnimationTime < 30) this.clientAnimationTime += 2;
                } else this.clientAnimationTime = Math.max(0, this.clientAnimationTime - 10);
                if (this.tickCount % 4 == 0) {
                    this.level().addParticle(EnigmaticAddonParticles.ICHOR, this.getRandomX(0.5), this.getY() + this.getRandom().nextFloat(), this.getRandomZ(0.5), 0.01, 0.01, 0.01);
                }
            }
            this.setAttackTimer(Math.min(64, attackTimer + 1));
            if ((this.getOwner() == null || !this.getOwner().isAlive()) && !this.level().isClientSide) {
                if (this.level() instanceof ServerLevel level)
                    level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                this.discard();
            }
        }
        super.aiStep();
    }

    public void travel(Vec3 travelVector) {
        if (this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            } else {
                this.moveRelative(this.getSpeed(), travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
            }
        }
        this.calculateEntityAnimation(false);
    }

    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == getOwner()) return false;
        if (this.getAttackTimer() >= 60 && source.getEntity() != null) {
            int time = 0;
            while (time++ < 10) {
                Vec3 pos = new Vec3(getOffset(random, 1.6F), getOffset(random, 0.6F), getOffset(random, 1.6F));
                if (this.level().noCollision(this, this.getBoundingBox().move(pos))) {
                    if (this.level() instanceof ServerLevel server) {
                        server.sendParticles(EnigmaticAddonParticles.ICHOR, this.getX(), this.getY(), this.getZ(), 16, 0, 0, 0, 0.1);
                        server.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 8, 0, 0, 0, 0);
                    }
                    this.teleportTo(this.getX() + pos.x, this.getY() + pos.y, this.getZ() + pos.z);
                    this.setAttackTimer(0);
                    return false;
                }
            }
        }
        return super.hurt(source, amount * 0.5F);
    }

    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 16 && !this.level().isClientSide() && !this.isRemoved()) {
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                LivingEntity target = this.getTarget();
                if (target != null) {
                    target.addEffect(new MobEffectInstance(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT, 600, 1), this);
                }
            }
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    protected boolean canRide(Entity vehicle) {
        return false;
    }

    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

//    protected AABB getAttackBoundingBox() {
//        return super.getAttackBoundingBox().inflate(BEAM_RANGE - 2);
//    }

    protected void customServerAiStep() {
        this.level().getProfiler().push("ichorSpriteBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("ichorSpriteActivityUpdate");
        Ai.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof IchorSprite sprite) this.setOwner(sprite.getOwner());
    }

    public LivingEntity getOwner() {
        return this.getBrain().getMemory(EnigmaticAddonMemories.ICHOR_SPRITE_OWNER).orElse(null);
    }

    public void setOwner(LivingEntity owner) {
        this.getBrain().setMemory(EnigmaticAddonMemories.ICHOR_SPRITE_OWNER, owner);
    }

    @Nullable
    public LivingEntity getActiveAttackTarget() {
        if (!this.hasActiveAttackTarget()) return null;
        if (this.level().isClientSide) {
            if (this.clientSideCachedAttackTarget != null) {
                return this.clientSideCachedAttackTarget;
            } else {
                Entity entity = this.level().getEntity(this.entityData.get(DATA_ID_ATTACK_TARGET));
                if (entity instanceof LivingEntity) {
                    this.clientSideCachedAttackTarget = (LivingEntity) entity;
                    return this.clientSideCachedAttackTarget;
                } else return null;
            }
        }
        return this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    void setActiveAttackTarget(int activeAttackTargetId) {
        if (this.clientSideCachedAttackTarget != this.level().getEntity(this.entityData.get(DATA_ID_ATTACK_TARGET))) {
            this.clientSideCachedAttackTarget = null;
        }
        this.entityData.set(DATA_ID_ATTACK_TARGET, activeAttackTargetId);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ID_ATTACK_TARGET.equals(key)) {
            this.clientSideCachedAttackTarget = null;
        }
    }

    public float getAttackAnimationScale(float partialTick) {
        float time = Mth.lerp(partialTick, this.clientAnimationTime0, this.clientAnimationTime);
        return Math.min(1.0F, time / 27.0F);
    }

    public int getAttackTimer() {
        return this.entityData.get(DATA_ATTACK_TIMER);
    }

    public void setAttackTimer(int timer) {
        this.entityData.set(DATA_ATTACK_TIMER, timer);
    }

    public boolean hasActiveAttackTarget() {
        return this.entityData.get(DATA_ID_ATTACK_TARGET) != 0;
    }

    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (this.getMainHandItem().isEmpty() && this.isEffectiveAi() && this.getBrain().isActive(Activity.IDLE) && itemInHand.isEmpty()) {
            this.setItemInHand(InteractionHand.MAIN_HAND, itemInHand.copyWithCount(1));
            if (!player.getAbilities().instabuild) itemInHand.shrink(1);
            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    private static class FollowOwner extends Behavior<IchorSprite> {
        final int range;

        public FollowOwner(int range) {
            super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, EnigmaticAddonMemories.ICHOR_SPRITE_OWNER, MemoryStatus.VALUE_PRESENT), 1200);
            this.range = range;
        }

        protected boolean canStillUse(ServerLevel level, IchorSprite sprite, long gameTime) {
            return sprite.getOwner() != null && !sprite.getOwner().closerThan(sprite, range);
        }

        protected void tick(ServerLevel level, IchorSprite sprite, long gameTime) {
            LivingEntity owner = sprite.getOwner();
            if (owner != null && !owner.closerThan(sprite, range * 0.6)) {
                sprite.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(owner, true), 1.0F, range));
            }
        }
    }

    private static class PurifyItem extends Behavior<IchorSprite> {
        private int transTimer = 0;

        public PurifyItem() {
            super(ImmutableMap.of(EnigmaticAddonMemories.ICHOR_SPRITE_OWNER, MemoryStatus.VALUE_PRESENT), 1200);
        }

        protected boolean checkExtraStartConditions(ServerLevel level, IchorSprite entity) {
            LivingEntity owner = entity.getOwner();
            return owner != null && owner.isAlive() && BehaviorUtils.canSee(entity, owner) && !entity.getMainHandItem().isEmpty();
        }

        protected boolean canStillUse(ServerLevel level, IchorSprite sprite, long gameTime) {
            return sprite.getOwner() != null && !sprite.getMainHandItem().isEmpty();
        }

        protected void tick(ServerLevel level, IchorSprite sprite, long gameTime) {
            LivingEntity owner = sprite.getOwner();
            if (owner != null && !sprite.getMainHandItem().isEmpty()) {
                this.transTimer++;
                if (this.transTimer > 100) {
                    ItemStack copy = sprite.getMainHandItem().copy();
//                    copy.set(EnigmaticComponents.BLESSED, true);
                    BehaviorUtils.throwItem(sprite, copy, sprite.position());
                    sprite.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    sprite.swing(InteractionHand.MAIN_HAND);
                    this.transTimer = 0;
                    this.doStop(level, sprite, gameTime);
                }
            }
        }
    }

    private static class HealOwner extends Behavior<IchorSprite> {
        public HealOwner() {
            super(ImmutableMap.of(EnigmaticAddonMemories.ICHOR_SPRITE_OWNER, MemoryStatus.VALUE_PRESENT), 20);
        }

        protected boolean checkExtraStartConditions(ServerLevel level, IchorSprite sprite) {
            if (sprite.getBrain().isActive(Activity.FIGHT)) return false;
            LivingEntity owner = sprite.getOwner();
            if (owner == null || !owner.closerThan(sprite, 5))
                return false;
            return super.checkExtraStartConditions(level, sprite);
        }

        protected void start(ServerLevel level, IchorSprite sprite, long gameTime) {
            LivingEntity owner = sprite.getOwner();
            if (owner != null && sprite.tickCount % 4 == 0) {
                float amount = Math.max(0.1F, (owner.getMaxHealth() - owner.getHealth()) * 0.1F);
                owner.heal(Math.min(amount, Math.max(sprite.getHealth() / 5.0F, 1.0F)));
                if (owner.getRandom().nextBoolean() && sprite.tickCount % 40 == 0) {
                    owner.addEffect(new MobEffectInstance(EnigmaticAddonEffects.PURE_RESISTANCE_EFFECT, 100));
                }
            }
        }
    }

    private static class BeamAttack extends Behavior<IchorSprite> {
        private int attackTime = 0;
        private int randomMoveTime = 0;

        public BeamAttack() {
            super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200);
        }

        private static @Nullable LivingEntity getAttackTarget(LivingEntity entity) {
            return entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        }

        protected boolean checkExtraStartConditions(ServerLevel level, IchorSprite entity) {
            LivingEntity target = getAttackTarget(entity);
            return target != null && target.isAlive() && BehaviorUtils.canSee(entity, target);
        }

        protected boolean canStillUse(ServerLevel level, IchorSprite entity, long gameTime) {
            LivingEntity target = getAttackTarget(entity);
            return target != null && entity.isAlive() && entity.distanceToSqr(target) < BEAM_RANGE * BEAM_RANGE;
        }

        protected void start(ServerLevel level, IchorSprite sprite, long gameTime) {
            this.attackTime = -16;
            LivingEntity target = getAttackTarget(sprite);
            if (target != null)
                sprite.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
            sprite.hasImpulse = true;
        }

        protected void stop(ServerLevel level, IchorSprite sprite, long gameTime) {
            sprite.setActiveAttackTarget(0);
            sprite.setTarget(null);
            sprite.hasImpulse = true;
        }

        protected void tick(ServerLevel level, IchorSprite sprite, long gameTime) {
            LivingEntity target = getAttackTarget(sprite);
            if (target != null) {
                RandomSource random = sprite.getRandom();
                sprite.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
                if (!sprite.hasLineOfSight(target)) {
                    this.doStop(level, sprite, gameTime);
                } else {
                    int nextTimer = this.attackTime;
                    int attackTimer = sprite.getAttackTimer();
                    if (attackTimer > this.attackTime && this.attackTime > 0) {
                        nextTimer = (attackTimer + this.attackTime * 2) / 3;
                    }
                    nextTimer += 1 + sprite.random.nextInt(2);
                    if (nextTimer > 0 && nextTimer < 64) {
                        sprite.setActiveAttackTarget(target.getId());
                    } else if (nextTimer >= 64) {
                        float f = 1.0F;
                        MobEffectInstance effect = target.getEffect(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT);
                        if (effect != null) f += (effect.getAmplifier() + 1) * 0.4F;
                        if (sprite.level().getDifficulty() == Difficulty.HARD) f += 2.0F;
                        if (target instanceof Player player && SuperpositionHandler.isTheCursedOne(player)) f *= 1.6F;
//                        PacketDistributor.sendToPlayersNear(level, null, sprite.getX(), sprite.getY(), sprite.getZ(), 16,
//                                new IchorSpriteBeamPacket(sprite.getEyePosition(), target.position().add(0, target.getBbHeight() * 0.5, 0)));
                        if (target.hurt(sprite.damageSources().indirectMagic(sprite, sprite), f)) {
                            if (effect != null && random.nextBoolean()) {
                                target.addEffect(new MobEffectInstance(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT, 600, effect.getAmplifier() + 1), sprite);
                            } else {
                                target.addEffect(new MobEffectInstance(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT, 600), sprite);
                            }
                        }
                        nextTimer = 0;
                        sprite.setAttackTimer(0);
                        sprite.doHurtTarget(target);
                        this.doStop(level, sprite, gameTime);
                    }
                    this.attackTime = nextTimer;
                }
                randomMoveTime += random.nextInt(9);
                if (randomMoveTime > 100) {
                    randomMoveTime = 0;
                    BlockPos blockPos = sprite.blockPosition();
                    Vec3 pos = sprite.position().add(getOffset(random, 0.75F), getOffset(random, 0.36F), getOffset(random, 0.75F));
                    BlockPos containing = BlockPos.containing(pos);
                    if (!blockPos.equals(containing) && containing.distToCenterSqr(target.position()) < BEAM_RANGE - 1 && sprite.level().getBlockState(containing).isAir())
                        sprite.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(containing, 1.0F, 1));
                }
            }
        }
    }

    protected static class Ai {
        protected static Brain<?> makeBrain(IchorSprite sprite, Brain<IchorSprite> brain) {
            initCoreActivity(brain);
            initIdleActivity(brain);
            initFightActivity(sprite, brain);
            brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
            brain.setDefaultActivity(Activity.IDLE);
            brain.useDefaultActivity();
            return brain;
        }

        private static void initCoreActivity(Brain<IchorSprite> brain) {
            brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                    new Swim(0.8F),
                    new LookAtTargetSink(45, 90),
                    new MoveToTargetSink(),
                    new HealOwner()
            ));
        }

        private static void initIdleActivity(Brain<IchorSprite> brain) {
            brain.addActivity(Activity.IDLE, ImmutableList.of(
                    Pair.of(0, new FollowOwner(6)),
                    Pair.of(0, new PurifyItem()),
                    Pair.of(1, StartAttacking.create(Ai::getOwnerTarget)),
                    Pair.of(2, StartAttacking.create(Ai::findNearestValidAttackTarget)),
                    Pair.of(3, StartAttacking.create(Ai::getHurtBy)),
                    Pair.of(3, new RunOne<>(ImmutableList.of(
                            Pair.of(RandomStroll.fly(1.0F), 2),
                            Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2),
                            Pair.of(new DoNothing(30, 60), 1)
                    ))),
                    Pair.of(4, createIdleLookBehaviors())
            ));
        }

        private static void initFightActivity(IchorSprite sprite, Brain<IchorSprite> brain) {
            brain.addActivityWithConditions(Activity.FIGHT, ImmutableList.of(
                    Pair.of(0, StopAttackingIfTargetInvalid.create(entity -> !isNearestValidAttackTarget(sprite, entity) && getOwnerTarget(sprite).isEmpty())),
                    Pair.of(1, BackUpIfTooClose.create(BEAM_RANGE, 0.8F)),
                    Pair.of(2, new FollowOwner(9)),
                    Pair.of(2, new BeamAttack()),
                    Pair.of(3, createOutOfReach(1.0F))
            ), ImmutableSet.of(
                    Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT),
                    Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT)
            ));
        }

        private static RunOne<IchorSprite> createIdleLookBehaviors() {
            return new RunOne<>(ImmutableList.of(
                    Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1),
                    Pair.of(SetEntityLookTarget.create(EnigmaticAddonEntities.ICHOR_SPRITE, 8.0F), 1),
                    Pair.of(SetEntityLookTarget.create(8.0F), 1),
                    Pair.of(new DoNothing(30, 60), 1)
            ));
        }

        private static boolean isNearestValidAttackTarget(Mob mob, LivingEntity target) {
            return findNearestValidAttackTarget(mob).filter(entity -> entity == target && entity.isAlive()).isPresent();
        }

        private static Optional<? extends LivingEntity> getOwnerTarget(Mob mob) {
            Optional<LivingEntity> memory = mob.getBrain().getMemory(EnigmaticAddonMemories.ICHOR_SPRITE_OWNER);
            if (memory.isPresent()) {
                LivingEntity owner = memory.get();
                LivingEntity hurtByMob = owner.getLastHurtByMob();
                LivingEntity lastHurtMob = owner.getLastHurtMob();
                if (hurtByMob != null && hurtByMob != owner) return Optional.of(hurtByMob);
                if (owner instanceof Mob mobOwner && mobOwner.getTarget() != null)
                    return Optional.of(mobOwner.getTarget());
                if (lastHurtMob != owner) return Optional.ofNullable(lastHurtMob);
            }
            return Optional.empty();
        }

        private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Mob mob) {
            Optional<LivingEntity> owner = mob.getBrain().getMemory(EnigmaticAddonMemories.ICHOR_SPRITE_OWNER);
            Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(mob, MemoryModuleType.ANGRY_AT);
            if (optional.isPresent() && optional.get() != owner.orElse(null) && Sensor.isEntityAttackableIgnoringLineOfSight(mob, optional.get())) {
                return optional;
            } else {
                Optional<? extends LivingEntity> optional1 = getTargetIfWithinRange(mob);
                Optional<? extends LivingEntity> entity = optional1.isPresent() ? optional1 : mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
                return entity.orElse(null) != owner.orElse(null) ? entity : Optional.empty();
            }
        }

        public static Optional<? extends LivingEntity> getHurtBy(Mob mob) {
            Optional<LivingEntity> optional = mob.getBrain().getMemory(EnigmaticAddonMemories.ICHOR_SPRITE_OWNER);
            return mob.getBrain().getMemory(MemoryModuleType.HURT_BY).map(DamageSource::getEntity).filter((entity) -> entity instanceof LivingEntity && entity != optional.orElse(null)).map((entity) -> (LivingEntity) entity);
        }

        private static Optional<? extends LivingEntity> getTargetIfWithinRange(Mob mob) {
            return mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER).filter(entity -> entity.closerThan(mob, BEAM_RANGE));
        }

        protected static void updateActivity(IchorSprite sprite) {
            Brain<IchorSprite> brain = sprite.getBrain();
            brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
            sprite.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        }


        public static BehaviorControl<Mob> createOutOfReach(float speedModifier) {
            return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET), instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
                    .apply(instance, (walkTarget, tracker, entityAccessor, nearAccessor) -> (level, entity, time) -> {
                        LivingEntity livingentity = instance.get(entityAccessor);
                        Optional<NearestVisibleLivingEntities> optional = instance.tryGet(nearAccessor);
                        if (optional.isPresent() && optional.get().contains(livingentity) && BehaviorUtils.isWithinAttackRange(entity, livingentity, 1)) {
                            walkTarget.erase();
                        } else {
                            tracker.set(new EntityTracker(livingentity, true));
                            walkTarget.set(new WalkTarget(new EntityTracker(livingentity, true), speedModifier, 0));
                        }

                        return true;
                    }));
        }
    }
}

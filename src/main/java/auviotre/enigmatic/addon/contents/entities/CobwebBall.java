package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class CobwebBall extends Projectile {
    public CobwebBall(EntityType<? extends Projectile> type, Level world) {
        super(type, world);
    }

    public CobwebBall(Level world, Monster spider) {
        this(EnigmaticAddonEntities.COBWEB_BALL, world);
        this.setOwner(spider);
        this.setPos(spider.getX() - (double) (spider.getBbWidth() + 1.0F) * 0.5D * (double) Mth.sin(spider.yBodyRot * ((float) Math.PI / 180F)), spider.getEyeY() - (double) 0.1F, spider.getZ() + (double) (spider.getBbWidth() + 1.0F) * 0.5D * (double) Mth.cos(spider.yBodyRot * ((float) Math.PI / 180F)));
    }

    public void tick() {
        super.tick();
        for (int i = 0; i < 3; ++i) {
            double theta = Math.random() * 2 * Math.PI;
            double phi = (Math.random() - 0.5D) * Math.PI;
            double deltaX = 0.02D * Math.sin(theta) * Math.cos(phi);
            double deltaY = 0.02D * Math.sin(phi);
            double deltaZ = 0.02D * Math.cos(theta) * Math.cos(phi);
            double x = this.getX() + this.getDeltaMovement().x * i / 3;
            double y = this.getY() + this.getDeltaMovement().y * i / 3;
            double z = this.getZ() + this.getDeltaMovement().z * i / 3;
            this.level().addParticle(this.getParticle(), x, y, z, deltaX, deltaY, deltaZ);
        }
        Vec3 vec3 = this.getDeltaMovement();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitresult))
            this.onHit(hitresult);
        double d0 = this.getX() + vec3.x;
        double d1 = this.getY() + vec3.y;
        double d2 = this.getZ() + vec3.z;
        this.updateRotation();
        if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir)) {
            this.discard();
        } else if (this.isInWaterOrBubble()) {
            this.discard();
        } else {
            this.setDeltaMovement(vec3.scale(0.98F));
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.06F, 0.0D));
            }
            this.setPos(d0, d1, d2);
        }
    }

    private ItemParticleOption getParticle() {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Blocks.COBWEB));
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity entity = this.getOwner();
        if (entity instanceof Spider spider) {
            Entity target = hitResult.getEntity();
            if (target.hurt(this.damageSources().mobProjectile(this, spider), 0.1F)) {
                if (target == spider.getTarget() && target instanceof LivingEntity living) {
                    entity.level().playSound(null, target.blockPosition(), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS);
                    int difficulty = this.level().getDifficulty().getId();
                    if (this.random.nextInt(5) > difficulty || !entity.level().getBlockState(target.blockPosition()).isAir()) {
                        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 75, 3), spider);
                    } else {
                        entity.level().setBlock(target.blockPosition(), Blocks.COBWEB.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    protected void defineSynchedData() {
    }

    public void recreateFromPacket(ClientboundAddEntityPacket entityPacket) {
        super.recreateFromPacket(entityPacket);
        double xa = entityPacket.getXa();
        double ya = entityPacket.getYa();
        double za = entityPacket.getZa();

        for (int i = 0; i < 7; ++i) {
            double v = 0.4D + 0.1D * i;
            double theta = Math.random() * 2 * Math.PI;
            double phi = (Math.random() - 0.5D) * Math.PI;
            double deltaX = 0.02D * Math.sin(theta) * Math.cos(phi) + xa * v;
            double deltaY = 0.02D * Math.sin(phi) + ya;
            double deltaZ = 0.02D * Math.cos(theta) * Math.cos(phi) + za * v;
            this.level().addParticle(this.getParticle(), this.getX(), this.getY(), this.getZ(), deltaX, deltaY, deltaZ);
        }

        this.setDeltaMovement(xa, ya, za);
    }
}

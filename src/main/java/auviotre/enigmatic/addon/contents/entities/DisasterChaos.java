package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.contents.items.DisasterSword;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class DisasterChaos extends Projectile {
    public DisasterChaos(EntityType<? extends Projectile> type, Level world) {
        super(type, world);
    }

    public DisasterChaos(Level world, LivingEntity entity) {
        this(EnigmaticAddonEntities.DISASTER_CHAOS, world);
        this.setOwner(entity);
        this.setPos(entity.getX(), entity.getEyeY() - 0.1F, entity.getZ());
    }

    public void tick() {
        super.tick();
        if (this.tickCount > 40) this.discard();
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
        } else {
            this.setDeltaMovement(vec3.scale(0.98F));
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.02F, 0.0D));
            }
            this.setPos(d0, d1, d2);
        }
    }

    private SimpleParticleType getParticle() {
        return ParticleTypes.SQUID_INK;
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity entity = this.getOwner();
        if (entity instanceof Player player) {
            Entity target = hitResult.getEntity();
            target.hurt(this.damageSources().mobProjectile(this, player), (float) DisasterSword.rangeAttackDamage.getValue());
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

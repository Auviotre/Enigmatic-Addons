package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ThrownQuartzDagger extends AbstractSpear {
    public int inGroundTick;

    public ThrownQuartzDagger(EntityType<? extends ThrownQuartzDagger> type, Level world) {
        super(type, world, EnigmaticAddonItems.QUARTZ_DAGGER);
    }

    public ThrownQuartzDagger(LivingEntity entity, Level world) {
        super(EnigmaticAddonEntities.QUARTZ_DAGGER, entity, world, EnigmaticAddonItems.QUARTZ_DAGGER);
    }

    public ThrownQuartzDagger(Level world, double x, double y, double z) {
        super(EnigmaticAddonEntities.QUARTZ_DAGGER, x, y, z, world, EnigmaticAddonItems.QUARTZ_DAGGER);
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            Vec3 vec3 = this.getDeltaMovement().scale(0.2);
            double dx = vec3.x;
            double dy = vec3.y;
            double dz = vec3.z;
            this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), dx, dy, dz);
        } else {
            List<ItemEntity> entities = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(3));
            if (!entities.isEmpty() && this.getOwner() instanceof Player player) {
                for (ItemEntity entity : entities) {
                    entity.setPos(player.getEyePosition());
                }
            }
        }
        if (this.inGround) ++this.inGroundTick;
        if (this.tickCount > 25 && this.isNoGravity()) this.setNoGravity(false);
        if (this.inGroundTick == 2 || this.tickCount == 40) {
            for (int counter = 0; counter < 16 && this.level().isClientSide; counter++) {
                double theta = Math.random() * 2 * Math.PI;
                double phi = (Math.random() - 0.5D) * Math.PI;
                double dx = Math.cos(theta) * Math.cos(phi) * 0.08D;
                double dy = Math.sin(phi) * 0.08D;
                double dz = Math.sin(theta) * Math.cos(phi) * 0.08D;
                this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), dx, dy, dz);
            }
        } else if (this.inGroundTick > 2 || this.tickCount > 40) {
            this.discard();
        }
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        hitResult.getEntity().invulnerableTime /= 2;
        for (int counter = 0; counter < 16 && this.level().isClientSide; counter++) {
            double theta = Math.random() * 2 * Math.PI;
            double phi = (Math.random() - 0.5D) * Math.PI;
            double dx = Math.cos(theta) * Math.cos(phi) * 0.08D;
            double dy = Math.sin(phi) * 0.08D;
            double dz = Math.sin(theta) * Math.cos(phi) * 0.08D;
            this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), dx, dy, dz);
        }
    }

    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}

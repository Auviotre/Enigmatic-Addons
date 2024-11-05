package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.contents.items.IchorSpear;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownIchorSpear extends AbstractSpear {
    public ThrownIchorSpear(EntityType<? extends ThrownIchorSpear> type, Level world) {
        super(type, world, EnigmaticAddonItems.ICHOR_SPEAR);
    }

    public ThrownIchorSpear(LivingEntity entity, Level world) {
        super(EnigmaticAddonEntities.ICHOR_SPEAR, entity, world, EnigmaticAddonItems.ICHOR_SPEAR);
    }

    public ThrownIchorSpear(Level world, double x, double y, double z) {
        super(EnigmaticAddonEntities.ICHOR_SPEAR, x, y, z, world, EnigmaticAddonItems.ICHOR_SPEAR);
    }

    protected void doPostHurtEffects(LivingEntity living) {
        super.doPostHurtEffects(living);
        Entity owner = this.getOwner();
        living.setSecondsOnFire(5);
        living.addEffect(new MobEffectInstance(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT, IchorSpear.duration.getValue(), IchorSpear.amplifier.getValue()), owner == null ? this : owner);
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide && !this.inGround) {
            Vec3 vec3 = this.getDeltaMovement();
            double dx = vec3.x;
            double dy = vec3.y;
            double dz = vec3.z;
            double length = vec3.length() * 1.25D;
            for (int i = 0; i < length; ++i) {
                this.level().addParticle(EnigmaticAddonParticles.ICHOR, this.getRandomX(0.0F) + dx * (double) i / length, this.getRandomY() + dy * (double) i / length, this.getRandomZ(0.0F) + dz * (double) i / length, -dx * 0.1, -dy * 0.1, -dz * 0.1);
            }
        }
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity();
        Entity owner = this.getOwner();
        double damage = IchorSpear.baseDamage.getValue() * Math.sqrt(this.getDeltaMovement().length());
        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);
        if (entity.hurt(damageSource, (float) damage)) {
            if (entity.getType() == EntityType.ENDERMAN) return;
            if (entity instanceof LivingEntity living) this.doPostHurtEffects(living);
        }
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 0.1F);
        this.discard();
    }

    protected ItemStack getPickupItem() {
        return EnigmaticAddonItems.ICHOR_SPEAR.getDefaultInstance();
    }
}

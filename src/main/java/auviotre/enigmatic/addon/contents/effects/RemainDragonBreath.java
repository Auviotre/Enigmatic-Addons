package auviotre.enigmatic.addon.contents.effects;

import auviotre.enigmatic.addon.contents.items.DragonBow;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class RemainDragonBreath extends InstantenousMobEffect {
    public RemainDragonBreath() {
        super(MobEffectCategory.BENEFICIAL, 0xdf61ff);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "744bf4f9-0647-49a9-9f6c-be42d42faeee", -0.125F, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(entity.damageSources().dragonBreath(), (float) (4 << amplifier));
    }

    public void applyInstantenousEffect(@Nullable Entity undirected, @Nullable Entity owner, LivingEntity target, int amplifier, double multiplier) {
        float damage = (int) (multiplier * (double) (4 << amplifier) + 0.5);
        if (owner == target) {
            damage = damage * DragonBow.ownerResistance.getValue().asModifierInverted();
            if (damage <= 0) return;
        }
        if (undirected == null) target.hurt(target.damageSources().dragonBreath(), damage);
        else target.hurt(target.damageSources().source(DamageTypes.DRAGON_BREATH, undirected, owner), damage);
        target.setDeltaMovement(Vec3.ZERO);
        target.hasImpulse = true;
        List<AreaEffectCloud> effectClouds = target.level().getEntitiesOfClass(AreaEffectCloud.class, target.getBoundingBox().inflate(4.0, 2.0, 4.0));
        if (!effectClouds.isEmpty()) {
            for (AreaEffectCloud cloud : effectClouds) {
                if (cloud.distanceToSqr(target) < cloud.getRadius() * cloud.getRadius() && target != cloud.getOwner()) {
                    Vec3 movement = target.getDeltaMovement().scale(0.5D);
                    Vec3 newPos = cloud.position().add(target.position().subtract(cloud.position()).scale(0.5D));
                    cloud.setPos(newPos.x + movement.x, newPos.y + movement.y, newPos.z + movement.z);
                    break;
                }
            }
        }
    }
}

package auviotre.enigmatic.addon.mixin.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public abstract class MixinFireworkRocketEntity extends Projectile implements ItemSupplier {
    protected MixinFireworkRocketEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    protected abstract boolean isAttachedToEntity();

    @Inject(method = "tick", at = @At("TAIL"))
    public void tickMix(CallbackInfo ci) {
        if (!this.isAttachedToEntity()) {
            if (this.getOwner() != null && this.getOwner().getClass() == Pillager.class) {
                Vec3 vec3 = this.getDeltaMovement().add(0, -0.044, 0);
                this.setDeltaMovement(vec3);
            }
        }
    }
}

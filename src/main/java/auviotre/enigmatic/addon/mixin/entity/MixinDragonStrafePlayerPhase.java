package auviotre.enigmatic.addon.mixin.entity;

import auviotre.enigmatic.addon.contents.entities.UltimateDragonFireball;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DragonStrafePlayerPhase.class)
public abstract class MixinDragonStrafePlayerPhase extends AbstractDragonPhaseInstance {
    public MixinDragonStrafePlayerPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @ModifyArg(method = "doServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public Entity Fireball(Entity entity) {
        RandomSource random = this.dragon.getRandom();
        int boostLevel = SuperAddonHandler.getEnderDragonBoostLevel(this.dragon);
        if (entity instanceof DragonFireball dfb && SuperAddonHandler.isCurseBoosted(this.dragon) && random.nextInt(5) < boostLevel) {
            Vec3 position = dfb.position().add(this.dragon.getDeltaMovement());
            UltimateDragonFireball fireball = new UltimateDragonFireball(this.dragon.level(), this.dragon, dfb.xPower, dfb.yPower, dfb.zPower);
            fireball.moveTo(position.x, position.y, position.z, 0.0F, 0.0F);
            fireball.setDeltaMovement(this.dragon.getDeltaMovement());
            Vec3 vec3 = new Vec3(dfb.xPower, dfb.yPower, dfb.zPower);
            for (int i = 0; i < boostLevel / 2; i++) {
                if (random.nextInt(3) == 0) continue;
                double x = 0.1F * (random.nextFloat() - 0.5F) * vec3.x / vec3.length();
                double y = 0.1F * (random.nextFloat() - 0.5F) * vec3.y / vec3.length();
                double z = 0.1F * (random.nextFloat() - 0.5F) * vec3.z / vec3.length();
                AbstractHurtingProjectile copy;
                if (random.nextInt(3) == 0)
                    copy = new DragonFireball(this.dragon.level(), this.dragon, dfb.xPower + x, dfb.yPower + y, dfb.zPower + z);
                else
                    copy = new UltimateDragonFireball(this.dragon.level(), this.dragon, dfb.xPower + x, dfb.yPower + y, dfb.zPower + z);
                copy.moveTo(position.x, position.y, position.z, 0.0F, 0.0F);
                copy.setDeltaMovement(this.dragon.getDeltaMovement());
                this.dragon.level().addFreshEntity(copy);
            }
            return fireball;
        }
        return entity;
    }
}

package auviotre.enigmatic.addon.mixin.entity;

import auviotre.enigmatic.addon.contents.entities.UltimateDragonFireball;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
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
        if (entity instanceof DragonFireball dragonFireball && SuperAddonHandler.isCurseBoosted(this.dragon) && this.dragon.getRandom().nextInt(2) == 0) {
            Vec3 position = dragonFireball.position();
            UltimateDragonFireball fireball = new UltimateDragonFireball(this.dragon.level(), this.dragon, dragonFireball.xPower, dragonFireball.yPower, dragonFireball.zPower);
            fireball.moveTo(position.x, position.y, position.z, 0.0F, 0.0F);
            return fireball;
        }
        return entity;
    }
}

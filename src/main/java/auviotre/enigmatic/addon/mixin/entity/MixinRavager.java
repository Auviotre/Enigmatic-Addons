package auviotre.enigmatic.addon.mixin.entity;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Ravager.class)
public abstract class MixinRavager extends Raider {
    protected MixinRavager(EntityType<? extends Raider> type, Level level) {
        super(type, level);
    }

    @Inject(method = "roar", at = @At("HEAD"))
    public void roarMix(CallbackInfo ci) {
        if (this.isAlive() && SuperAddonHandler.isCurseBoosted(this)) {
            List<Raider> entities = this.level().getEntitiesOfClass(Raider.class, this.getBoundingBox().inflate(4.0), LivingEntity::isAlive);
            for (Raider entity : entities) {
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 900));
            }
        }
    }
}

package auviotre.enigmatic.addon.mixin.entity;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Husk.class)
public abstract class MixinHusk extends Zombie {
    protected MixinHusk(EntityType<? extends Zombie> type, Level world) {
        super(type, world);
    }

    @Inject(method = "doHurtTarget", at = @At("RETURN"))
    public void doHurtTargetMix(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && SuperAddonHandler.isCurseBoosted(this) && this.getMainHandItem().isEmpty() && entity instanceof LivingEntity living) {
            float effectiveDifficulty = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100 * (int) effectiveDifficulty), this);
        }
    }
}

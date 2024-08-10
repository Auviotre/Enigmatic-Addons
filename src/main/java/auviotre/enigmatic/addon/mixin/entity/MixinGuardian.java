package auviotre.enigmatic.addon.mixin.entity;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Guardian.class)
public abstract class MixinGuardian extends Monster {
    protected MixinGuardian(EntityType<? extends Monster> type, Level world) {
        super(type, world);
    }

    @Inject(method = "getAttackDuration", at = @At("RETURN"), cancellable = true)
    public void getDurationMix(CallbackInfoReturnable<Integer> cir) {
        if (SuperAddonHandler.isCurseBoosted(this)) {
            cir.setReturnValue(cir.getReturnValue() * Mth.floor(this.getRandom().nextFloat() * 2 + 1) / 3);
        }
    }
}

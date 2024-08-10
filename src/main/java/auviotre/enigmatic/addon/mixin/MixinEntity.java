package auviotre.enigmatic.addon.mixin;

import net.minecraft.commands.CommandSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity extends CapabilityProvider<Entity> implements Nameable, EntityAccess, CommandSource, IForgeEntity {
    protected MixinEntity(Class<Entity> baseClass) {
        super(baseClass);
    }

    @Shadow
    public abstract int getTicksRequiredToFreeze();

    @Shadow
    public abstract int getTicksFrozen();

    @Inject(method = "getTicksRequiredToFreeze", at = @At("RETURN"), cancellable = true)
    public void getTicksRequiredToFreezeMix(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + 400);
    }

    @Inject(method = "getPercentFrozen", at = @At("RETURN"), cancellable = true)
    public void getPercentFrozenMix(CallbackInfoReturnable<Float> cir) {
        int i = this.getTicksRequiredToFreeze() - 400;
        cir.setReturnValue((float) Math.min(this.getTicksFrozen(), i) / (float) i);
    }

    @Inject(method = "isFullyFrozen", at = @At("RETURN"), cancellable = true)
    public void isFullyFrozenMix(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.getTicksFrozen() >= this.getTicksRequiredToFreeze() - 400);
    }
}

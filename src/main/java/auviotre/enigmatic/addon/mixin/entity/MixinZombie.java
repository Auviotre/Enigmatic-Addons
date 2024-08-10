package auviotre.enigmatic.addon.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class MixinZombie extends Monster {
    protected MixinZombie(EntityType<? extends Monster> type, Level world) {
        super(type, world);
    }

    @Inject(method = "doHurtTarget", at = @At("RETURN"))
    public void doHurtTargetMix(Entity entity, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            float effectiveDifficulty = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            if (this.getMainHandItem().is(Items.TORCH) && this.random.nextInt(8) == 0) {
                entity.setSecondsOnFire((int) (2 * effectiveDifficulty));
            }
        }
    }
}

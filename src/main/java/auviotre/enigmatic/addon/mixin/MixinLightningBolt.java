package auviotre.enigmatic.addon.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public abstract class MixinLightningBolt extends Entity {
    public MixinLightningBolt(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "spawnFire", at = @At("HEAD"), cancellable = true)
    public void mixSpawnFire(int range, CallbackInfo ci) {
        if (this.getTags().contains("HarmlessThunder")) ci.cancel();
    }
}

package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity {
    protected MixinPlayer(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), index = 1, argsOnly = true)
    public DamageSource hurtMix(DamageSource source) {
        if (this.self() instanceof Player player && SuperpositionHandler.isTheCursedOne(player) && SuperpositionHandler.hasItem(player, EnigmaticAddonItems.FALSE_JUSTICE)) {
            return SuperAddonHandler.damageSource(this, EnigmaticAddonDamageTypes.FALSE_JUSTICE, source.getDirectEntity(), source.getEntity());
        }
        return source;
    }

    @Inject(method = "sweepAttack", at = @At("TAIL"))
    public void sweepMix(CallbackInfo ci) {
        if (this.getPersistentData().getBoolean("DisasterCounterattack")) {
            List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5));
            for (LivingEntity entity : entities) {
                if (entity.is(this)) continue;
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 3));
                entity.addDeltaMovement(new Vec3(0, -2, 0));
            }
        }
    }
}

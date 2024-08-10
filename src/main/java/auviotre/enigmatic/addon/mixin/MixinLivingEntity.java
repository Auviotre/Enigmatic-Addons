package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.handlers.OmniconfigAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements Attackable, IForgeLivingEntity {
    @Shadow
    public abstract boolean canFreeze();

    public MixinLivingEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tickMix(CallbackInfo ci) {
        if (this.level().isClientSide && OmniconfigAddonHandler.frostParticle.getValue() && this.isFullyFrozen() && Math.random() > 0.25D) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getRandomX(0.6D), this.getRandomY() + 0.1D, this.getRandomZ(0.6D), 0.0D, -0.05D, 0.0D);
        }
        if (!this.canFreeze() && this.getTicksFrozen() > 0) this.setTicksFrozen(0);
    }

    @Inject(method = "canFreeze", at = @At("RETURN"), cancellable = true)
    public void canFreezeMix(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && !this.self().hasEffect(EnigmaticAddonEffects.FROZEN_HEART_EFFECT) && !SuperpositionHandler.hasCurio(this.self(), EnigmaticAddonItems.FORGOTTEN_ICE));
    }

    @Inject(method = "onChangedBlock", at = @At("HEAD"))
    public void onChangedBlockMix(BlockPos pos, CallbackInfo ci) {
        if (SuperpositionHandler.hasCurio(this.self(), EnigmaticAddonItems.FORGOTTEN_ICE)) {
            FrostWalkerEnchantment.onEntityMoved(this.self(), this.level(), pos, 1);
        }
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), index = 1, argsOnly = true)
    public DamageSource hurtMix(DamageSource source) {
        Entity entity = source.getEntity();
        if (entity instanceof Player player && SuperpositionHandler.isTheCursedOne(player) && SuperpositionHandler.hasItem(player, EnigmaticAddonItems.FALSE_JUSTICE)) {
            return player.damageSources().source(EnigmaticAddonDamageTypes.FALSE_JUSTICE, source.getDirectEntity(), player);
        }
        return source;
    }
}

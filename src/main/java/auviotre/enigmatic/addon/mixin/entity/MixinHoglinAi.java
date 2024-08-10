package auviotre.enigmatic.addon.mixin.entity;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(HoglinAi.class)
public class MixinHoglinAi {

    @Inject(
            at = {@At("RETURN")},
            method = {"findNearestValidAttackTarget"},
            cancellable = true
    )
    private static void onHoglinShallAttack(Hoglin hoglin, CallbackInfoReturnable<Optional<? extends LivingEntity>> info) {
        Optional<? extends LivingEntity> returnedTarget = info.getReturnValue();
        if (returnedTarget.isPresent() && returnedTarget.orElse(null) instanceof Player player) {
            if (SuperpositionHandler.hasItem(player, EnigmaticAddonItems.LIVING_ODE)) {
                info.setReturnValue(Optional.empty());
            }
        }
    }
}

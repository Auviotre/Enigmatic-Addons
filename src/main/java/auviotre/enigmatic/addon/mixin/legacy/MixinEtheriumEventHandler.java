package auviotre.enigmatic.addon.mixin.legacy;

import com.aizistral.etherium.core.EtheriumEventHandler;
import com.aizistral.etherium.core.IEtheriumConfig;
import com.aizistral.etherium.items.EtheriumArmor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EtheriumEventHandler.class)
public class MixinEtheriumEventHandler {
    @Shadow(remap = false)
    @Final
    private IEtheriumConfig config;

    @Inject(method = "onEntityHurt", at = @At(value = "HEAD"), remap = false, cancellable = true)
    public void onEntityHurtMix(LivingHurtEvent event, CallbackInfo ci) {
        if (event.getEntity() instanceof Player player) {
            if (event.getAmount() > 0.0F && EtheriumArmor.hasShield(player)) {
                if (event.getSource().getDirectEntity() instanceof LivingEntity attacker && event.getSource().getEntity() == null) {
                    event.setAmount(event.getAmount() * this.config.getShieldReduction().asModifierInverted());
                    ci.cancel();
                }
            }
        }
    }
}

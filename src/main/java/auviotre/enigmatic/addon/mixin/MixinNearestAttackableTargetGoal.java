package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class MixinNearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
    @Shadow
    @Final
    protected Class<T> targetType;

    public MixinNearestAttackableTargetGoal(Mob mob, boolean flag) {
        super(mob, flag);
    }

    @ModifyArg(
            method = "<init>(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;selector(Ljava/util/function/Predicate;)Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;")
    )
    public Predicate<LivingEntity> findTargetMix(Predicate<LivingEntity> selector) {
        if (this.targetType == Player.class || this.targetType == ServerPlayer.class) {
            if (this.mob instanceof Animal && this.mob instanceof NeutralMob) {
                return (player) -> !SuperpositionHandler.hasItem((Player) player, EnigmaticAddonItems.LIVING_ODE) && (selector == null || selector.test(player));
            }
            if (this.mob instanceof AbstractGolem) {
                return (player) -> !SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.LOST_ENGINE) && (selector == null || selector.test(player));
            }
        }
        return selector;
    }
}

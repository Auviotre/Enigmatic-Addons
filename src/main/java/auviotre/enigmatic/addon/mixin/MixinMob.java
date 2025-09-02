package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.contents.items.ExtradimensionalScepter;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MixinMob extends LivingEntity {
    protected MixinMob(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "checkAndHandleImportantInteractions", at = @At("HEAD"), cancellable = true)
    public void mixImportantInteractions(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(EnigmaticAddonItems.EXTRADIMENSIONAL_SCEPTER) && !ExtradimensionalScepter.Helper.isCombatMode(stack)) {
            InteractionResult result = stack.interactLivingEntity(player, this, hand);
            if (result.consumesAction()) cir.setReturnValue(result);
        }
    }
}

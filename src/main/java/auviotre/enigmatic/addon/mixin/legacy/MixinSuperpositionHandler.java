package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.contents.items.AntiqueBag;
import auviotre.enigmatic.addon.contents.items.BlessRing;
import auviotre.enigmatic.addon.contents.items.ViolenceScroll;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(SuperpositionHandler.class)
public abstract class MixinSuperpositionHandler {

    @Inject(method = "getCurseAmount(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true, remap = false)
    private static void getCurseAmountMix(@NotNull ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.is(EnigmaticAddonItems.HELL_BLADE_CHARM)) {
            cir.setReturnValue(cir.getReturnValue() + 2);
        } else if (stack.is(EnigmaticAddonItems.VIOLENCE_SCROLL)) {
            cir.setReturnValue(cir.getReturnValue() + ViolenceScroll.Helper.getCurseCount(stack));
        }
    }

    @Inject(method = "hasItem", at = @At("RETURN"), cancellable = true, remap = false)
    private static void hasItemMix(Player player, Item item, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && player != null && AntiqueBag.isBook(new ItemStack(item))) {
            cir.setReturnValue(!SuperAddonHandler.findBookInBag(player, item).isEmpty());
        }
    }

    @Inject(method = "isTheCursedOne", at = @At("RETURN"), cancellable = true, remap = false)
    private static void isCursed(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.getPersistentData().getBoolean(BlessRing.CURSED_SPAWN)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isTheWorthyOne", at = @At("RETURN"), cancellable = true, remap = false)
    private static void isWorthy(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.getPersistentData().getBoolean(BlessRing.WORTHY_SPAWN)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getObservedEntities", at = @At("RETURN"), cancellable = true, remap = false)
    private static void getObservedEntitiesMix(Player player, Level world, float range, int maxDist, boolean stopWhenFound, CallbackInfoReturnable<List<LivingEntity>> cir) {
        List<LivingEntity> returnValue = cir.getReturnValue();
        returnValue.removeIf(entity -> entity instanceof OwnableEntity own && own.getOwner() == player && !(entity instanceof TamableAnimal pet && !pet.isTame()));
        cir.setReturnValue(returnValue);
    }
}

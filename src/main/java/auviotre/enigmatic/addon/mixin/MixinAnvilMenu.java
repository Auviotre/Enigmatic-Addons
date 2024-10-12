package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class MixinAnvilMenu extends ItemCombinerMenu {
    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    public abstract int getCost();

    public MixinAnvilMenu(@Nullable MenuType<?> menuType, int id, Inventory inventory, ContainerLevelAccess level) {
        super(menuType, id, inventory, level);
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V"))
    public void createResultMix(CallbackInfo ci) {
        if (this.player != null && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.FORGER_GEM))
            this.cost.set((this.getCost() + 1) / 2);
    }
}

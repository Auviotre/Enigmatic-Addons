package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.contents.items.BlessRing;
import com.aizistral.enigmaticlegacy.api.items.ITaintable;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(ITaintable.class)
public interface MixinITaintable extends ITaintable {
    /**
     * @author Auviotre
     * @reason Compat with the Redemption.
     */
    @Overwrite(remap = false)
    default void handleTaintable(ItemStack stack, Player player) {
        if (SuperpositionHandler.isTheCursedOne(player) || BlessRing.Helper.betrayalAvailable(player)) {
            if (!ItemNBTHelper.getBoolean(stack, "isTainted", false)) {
                ItemNBTHelper.setBoolean(stack, "isTainted", true);
            }
        } else if (ItemNBTHelper.getBoolean(stack, "isTainted", false)) {
            ItemNBTHelper.setBoolean(stack, "isTainted", false);
        }
    }
}

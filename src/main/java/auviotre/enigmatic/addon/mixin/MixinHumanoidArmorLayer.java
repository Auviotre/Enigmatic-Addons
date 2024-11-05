package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
@Mixin(HumanoidArmorLayer.class)
public class MixinHumanoidArmorLayer {
    @Shadow
    @Final
    private static Map<String, ResourceLocation> ARMOR_LOCATION_CACHE;

    @Inject(method = "getArmorResource", at = @At("RETURN"), cancellable = true, remap = false)
    private void getArmorTextureMix(Entity entity, ItemStack stack, EquipmentSlot slot, String type, CallbackInfoReturnable<ResourceLocation> cir) {
        if (entity instanceof Player player && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.ETHERIUM_CORE)) {
            ItemStack curioStack = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.ETHERIUM_CORE);
            if (ItemNBTHelper.getBoolean(curioStack, "ArmorInvisibility", false)) {
                String armor = "enigmaticlegacy:textures/models/armor/unseen_armor.png";
                ResourceLocation resourcelocation = ARMOR_LOCATION_CACHE.get(armor);
                if (resourcelocation == null) {
                    resourcelocation = new ResourceLocation(armor);
                    ARMOR_LOCATION_CACHE.put(armor, resourcelocation);
                }
                cir.setReturnValue(resourcelocation);
            }
        }
    }
}

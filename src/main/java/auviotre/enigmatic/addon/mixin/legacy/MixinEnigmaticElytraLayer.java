package auviotre.enigmatic.addon.mixin.legacy;

import com.aizistral.enigmaticlegacy.client.renderers.EnigmaticElytraLayer;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;

@Pseudo
@Mixin(EnigmaticElytraLayer.class)
public abstract class MixinEnigmaticElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public MixinEnigmaticElytraLayer(RenderLayerParent<T, M> layerParent) {
        super(layerParent);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"), cancellable = true, remap = false)
    public void renderMix(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T livingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
        ItemStack stack = SuperpositionHandler.getEnigmaticElytra(livingEntity);
        if (SuperpositionHandler.hasCurio(livingEntity, EnigmaticItems.ENIGMATIC_ELYTRA)) {
            AtomicBoolean flag = new AtomicBoolean(false);
            CuriosApi.getCuriosInventory(livingEntity).ifPresent((handler) -> handler.findFirstCurio((itemStack) -> itemStack.is(EnigmaticItems.ENIGMATIC_ELYTRA)).ifPresent(curio -> {
                if (!curio.slotContext().visible()) flag.set(true);
            }));
            if (flag.get()) ci.cancel();
        }
    }
}

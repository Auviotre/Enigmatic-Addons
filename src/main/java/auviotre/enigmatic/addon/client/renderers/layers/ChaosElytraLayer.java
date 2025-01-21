package auviotre.enigmatic.addon.client.renderers.layers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;

@OnlyIn(Dist.CLIENT)
public class ChaosElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(EnigmaticAddons.MODID, "textures/item/3d/chaos_elytra.png");
    private final ElytraModel<T> elytraModel;

    public ChaosElytraLayer(RenderLayerParent<T, M> layerParent, EntityModelSet modelSet) {
        super(layerParent);
        this.elytraModel = new ElytraModel<>(modelSet.bakeLayer(ModelLayers.ELYTRA));
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = SuperAddonHandler.getChaosElytra(livingEntity);
        if (SuperpositionHandler.hasCurio(livingEntity, EnigmaticAddonItems.CHAOS_ELYTRA)) {
            AtomicBoolean flag = new AtomicBoolean(false);
            CuriosApi.getCuriosInventory(livingEntity).ifPresent((handler) -> handler.findFirstCurio((itemStack) -> itemStack.is(EnigmaticAddonItems.CHAOS_ELYTRA)).ifPresent(curio -> {
                if (!curio.slotContext().visible()) flag.set(true);
            }));
            if (flag.get()) return;
        }
        if (stack != null) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 0.125);
            this.getParentModel().copyPropertiesTo(this.elytraModel);
            this.elytraModel.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(TEXTURE), false, stack.isEnchanted());
            float red = 1.0F;
            float green = 1.0F;
            float blue = 1.0F;
            float alpha = 1.0F;
            this.elytraModel.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
            poseStack.popPose();
        }
    }
}

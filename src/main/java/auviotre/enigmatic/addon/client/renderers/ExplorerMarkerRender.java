package auviotre.enigmatic.addon.client.renderers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.client.renderers.models.ExplorerMarkerModel;
import auviotre.enigmatic.addon.contents.entities.ExplorerMarker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExplorerMarkerRender extends EntityRenderer<ExplorerMarker> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(EnigmaticAddons.MODID, "textures/models/misc/explorer_marker.png");
    private final ExplorerMarkerModel model;

    public ExplorerMarkerRender(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ExplorerMarkerModel(context.bakeLayer(ExplorerMarkerModel.LAYER));
    }

    public void render(ExplorerMarker entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.outline(getTextureLocation(entity)));
        this.model.render(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.0F);
        poseStack.popPose();
    }


    public ResourceLocation getTextureLocation(ExplorerMarker entity) {
        return TEXTURE;
    }
}

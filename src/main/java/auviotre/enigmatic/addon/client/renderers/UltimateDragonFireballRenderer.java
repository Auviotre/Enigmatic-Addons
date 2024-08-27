package auviotre.enigmatic.addon.client.renderers;

import auviotre.enigmatic.addon.contents.entities.UltimateDragonFireball;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class UltimateDragonFireballRenderer extends EntityRenderer<UltimateDragonFireball> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);
    ;

    public UltimateDragonFireballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    protected int getBlockLightLevel(UltimateDragonFireball entity, BlockPos pos) {
        return 15;
    }

    public void render(UltimateDragonFireball entity, float p_114081_, float p_114082_, PoseStack poseStack, MultiBufferSource buffer, int p_114085_) {
        poseStack.pushPose();
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        Matrix3f normal = last.normal();
        VertexConsumer $$9 = buffer.getBuffer(RENDER_TYPE);
        vertex($$9, pose, normal, p_114085_, 0.0F, 0, 0, 1);
        vertex($$9, pose, normal, p_114085_, 1.0F, 0, 1, 1);
        vertex($$9, pose, normal, p_114085_, 1.0F, 1, 1, 0);
        vertex($$9, pose, normal, p_114085_, 0.0F, 1, 0, 0);
        poseStack.popPose();
        super.render(entity, p_114081_, p_114082_, poseStack, buffer, p_114085_);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f, int p_253829_, float p_253995_, int p_254031_, int p_253641_, int p_254243_) {
        consumer.vertex(matrix4f, p_253995_ - 0.5F, (float) p_254031_ - 0.25F, 0.0F).color(255, 255, 255, 255).uv((float) p_253641_, (float) p_254243_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_253829_).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
    }

    public ResourceLocation getTextureLocation(UltimateDragonFireball entity) {
        return TEXTURE_LOCATION;
    }
}

package auviotre.enigmatic.addon.client.renderers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.client.renderers.models.IchorSpriteModel;
import auviotre.enigmatic.addon.contents.entities.IchorSprite;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class IchorSpriteRenderer extends MobRenderer<IchorSprite, IchorSpriteModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(EnigmaticAddons.MODID, "textures/entity/ichor_sprite.png");
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation(EnigmaticAddons.MODID, "textures/entity/ichor_sprite_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entityTranslucentCull(BEAM_LOCATION);

    public IchorSpriteRenderer(EntityRendererProvider.Context context) {
        super(context, new IchorSpriteModel(context.bakeLayer(IchorSpriteModel.LAYER)), 0.3F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f, float x, float y, float z, int alpha, float u, float v) {
        consumer.vertex(matrix4f, x, y, z).color(255, 255, 255, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
    }

    public void render(IchorSprite entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        LivingEntity target = entity.getActiveAttackTarget();
        if (target != null) {
            float scale = entity.getAttackAnimationScale(partialTicks);
            float timer = scale * 30.0F;
            float f2 = timer * 0.5F % 1.0F;
            float eyeHeight = entity.getEyeHeight();
            poseStack.pushPose();
            poseStack.translate(0.0F, eyeHeight, 0.0F);
            Vec3 tarPos = this.getPosition(target, target.getBbHeight() * 0.5, partialTicks);
            Vec3 selfPos = this.getPosition(entity, eyeHeight, partialTicks);
            Vec3 delta = tarPos.subtract(selfPos);
            float len = (float) (delta.length() + 0.25 * target.getBbWidth());
            delta = delta.normalize();
            float phi = (float) Math.acos(delta.y);
            float theta = (float) Math.atan2(delta.z, delta.x);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(Math.PI / 2 - theta)));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(phi)));

            float f7 = timer * 0.05F * -1.5F;
            float f8 = scale * scale;
            int alpha = Mth.clamp((int) (f8 * 256), 0, 255);
            float f11 = Mth.cos(f7 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
            float f12 = Mth.sin(f7 + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
            float f13 = Mth.cos(f7 + (float) (Math.PI / 4)) * 0.282F;
            float f14 = Mth.sin(f7 + (float) (Math.PI / 4)) * 0.282F;
            float f15 = Mth.cos(f7 + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
            float f16 = Mth.sin(f7 + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
            float f17 = Mth.cos(f7 + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
            float f18 = Mth.sin(f7 + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
            float f19 = Mth.cos(f7 + (float) Math.PI) * 0.2F;
            float f20 = Mth.sin(f7 + (float) Math.PI) * 0.2F;
            float f21 = Mth.cos(f7 + 0.0F) * 0.2F;
            float f22 = Mth.sin(f7 + 0.0F) * 0.2F;
            float f23 = Mth.cos(f7 + (float) (Math.PI / 2)) * 0.2F;
            float f24 = Mth.sin(f7 + (float) (Math.PI / 2)) * 0.2F;
            float f25 = Mth.cos(f7 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
            float f26 = Mth.sin(f7 + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
            float f29 = -1.0F + f2;
            float f30 = len * 2.5F + f29;
            VertexConsumer consumer = buffer.getBuffer(BEAM_RENDER_TYPE);
            PoseStack.Pose stack = poseStack.last();
            Matrix4f pose = stack.pose();
            Matrix3f normal = stack.normal();
            vertex(consumer, pose, normal, f19, len, f20, alpha, 0.4999F, f30);
            vertex(consumer, pose, normal, f19, 0.0F, f20, alpha, 0.4999F, f29);
            vertex(consumer, pose, normal, f21, 0.0F, f22, alpha, 0.0F, f29);
            vertex(consumer, pose, normal, f21, len, f22, alpha, 0.0F, f30);
            vertex(consumer, pose, normal, f23, len, f24, alpha, 0.4999F, f30);
            vertex(consumer, pose, normal, f23, 0.0F, f24, alpha, 0.4999F, f29);
            vertex(consumer, pose, normal, f25, 0.0F, f26, alpha, 0.0F, f29);
            vertex(consumer, pose, normal, f25, len, f26, alpha, 0.0F, f30);
            float yOffset = 0.0F;
            if (entity.tickCount % 2 == 0) yOffset = 0.5F;
            vertex(consumer, pose, normal, f11, len, f12, alpha, 0.5F, yOffset + 0.5F);
            vertex(consumer, pose, normal, f13, len, f14, alpha, 1.0F, yOffset + 0.5F);
            vertex(consumer, pose, normal, f17, len, f18, alpha, 1.0F, yOffset);
            vertex(consumer, pose, normal, f15, len, f16, alpha, 0.5F, yOffset);
            poseStack.popPose();
        }
    }

    protected int getBlockLightLevel(IchorSprite entity, BlockPos pos) {
        return 15;
    }

    public ResourceLocation getTextureLocation(IchorSprite entity) {
        return TEXTURE;
    }

    private Vec3 getPosition(LivingEntity entity, double yOffset, float partialTick) {
        double d0 = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double d1 = Mth.lerp(partialTick, entity.yOld, entity.getY()) + yOffset;
        double d2 = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        return new Vec3(d0, d1, d2);
    }
}

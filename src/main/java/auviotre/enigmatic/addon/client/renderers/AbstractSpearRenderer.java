package auviotre.enigmatic.addon.client.renderers;

import auviotre.enigmatic.addon.contents.entities.AbstractSpear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSpearRenderer<T extends AbstractSpear> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final Vec3 scale;
    private final float offset;

    public AbstractSpearRenderer(EntityRendererProvider.Context context, float offset, Vec3 scale) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.175F;
        this.offset = offset;
        this.scale = scale.scale(0.85);
    }

    public void render(T spear, float v, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        ItemStack itemstack = spear.getItem();
        BakedModel bakedmodel = this.itemRenderer.getModel(itemstack, spear.level(), null, spear.getId());

        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, spear.yRotO - 90.0F, spear.getYRot() - 90.0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, spear.xRotO - 45.0F, spear.getXRot() - 45.0F)));
        poseStack.translate(-offset, -offset, 0);
        poseStack.translate(0, spear.getBbHeight() / 2, 0);
        poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);

        this.itemRenderer.render(itemstack, ItemDisplayContext.NONE, false, poseStack, buffer, light, OverlayTexture.NO_OVERLAY, bakedmodel);
        poseStack.popPose();
        super.render(spear, v, partialTick, poseStack, buffer, light);
    }
}

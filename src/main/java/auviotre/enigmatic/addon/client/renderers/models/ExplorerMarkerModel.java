package auviotre.enigmatic.addon.client.renderers.models;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExplorerMarkerModel extends Model {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(EnigmaticLegacy.MODID, "explorer_marker"), "main");
    private final ModelPart main;

    public ExplorerMarkerModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.main = root.getChild("main");
    }


    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 32, 32);
    }


    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float r, float g, float b, float a) {
        this.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float r, float g, float b, float a) {
        this.main.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
    }
}

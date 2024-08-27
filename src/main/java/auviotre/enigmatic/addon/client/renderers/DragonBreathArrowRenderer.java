package auviotre.enigmatic.addon.client.renderers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.entities.DragonBreathArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DragonBreathArrowRenderer extends ArrowRenderer<DragonBreathArrow> {
    public DragonBreathArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(DragonBreathArrow dragonBreathArrow) {
        return new ResourceLocation(EnigmaticAddons.MODID, "textures/entity/projectiles/dragon_breath_arrow.png");
    }
}

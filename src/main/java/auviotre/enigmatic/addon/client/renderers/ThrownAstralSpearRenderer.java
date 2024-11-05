package auviotre.enigmatic.addon.client.renderers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.entities.ThrownAstralSpear;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownAstralSpearRenderer extends AbstractSpearRenderer<ThrownAstralSpear> {
    public ThrownAstralSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(ThrownAstralSpear spear) {
        return new ResourceLocation(EnigmaticAddons.MODID, "textures/item/3d/astral_spear.png");
    }
}

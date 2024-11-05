package auviotre.enigmatic.addon.client.renderers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.entities.ThrownIchorSpear;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownIchorSpearRenderer extends AbstractSpearRenderer<ThrownIchorSpear> {
    public ThrownIchorSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(ThrownIchorSpear spear) {
        return new ResourceLocation(EnigmaticAddons.MODID, "textures/item/3d/ichor_spear.png");
    }
}
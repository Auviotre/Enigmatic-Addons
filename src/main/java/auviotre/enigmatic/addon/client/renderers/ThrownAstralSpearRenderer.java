package auviotre.enigmatic.addon.client.renderers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.entities.ThrownAstralSpear;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownAstralSpearRenderer extends AbstractSpearRenderer<ThrownAstralSpear> {
    public ThrownAstralSpearRenderer(EntityRendererProvider.Context context) {
        super(context, 0.55F, new Vec3(2.0, 2.0, 1.0));
    }

    public ResourceLocation getTextureLocation(ThrownAstralSpear spear) {
        return new ResourceLocation(EnigmaticAddons.MODID, "textures/item/3d/astral_spear.png");
    }
}

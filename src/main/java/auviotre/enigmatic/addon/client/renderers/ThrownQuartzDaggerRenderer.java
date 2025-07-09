package auviotre.enigmatic.addon.client.renderers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.entities.ThrownQuartzDagger;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownQuartzDaggerRenderer extends AbstractSpearRenderer<ThrownQuartzDagger> {
    public ThrownQuartzDaggerRenderer(EntityRendererProvider.Context context) {
        super(context, 0.1F, new Vec3(0.8, 0.8, 0.8));
    }

    public ResourceLocation getTextureLocation(ThrownQuartzDagger spear) {
        return new ResourceLocation(EnigmaticAddons.MODID, "textures/item/3d/magic_quartz_dagger.png");
    }
}

package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(LightTexture.class)
public abstract class MixinLightTexture {
    @Final
    @Shadow
    private Minecraft minecraft;
    @Final
    @Shadow
    private DynamicTexture lightTexture;
    @Final
    @Shadow
    private NativeImage lightPixels;

    @Shadow
    private boolean updateLightTexture;

    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void getNightVisionScaleMix(float p_109882_, CallbackInfo ci) {
        if (this.updateLightTexture) {
            ClientLevel clientlevel = this.minecraft.level;
            if (clientlevel != null && SuperAddonHandler.isPunishedOne(this.minecraft.player)) {
                this.updateLightTexture = false;
                this.minecraft.getProfiler().push("lightTex");
                int punishment = SuperpositionHandler.getPersistentInteger(this.minecraft.player, "Punishment", 0);
                float process = Mth.clamp((float) punishment / 20.0F, 0.5F, 1.0F);
                int gray = (int) ((1 + Math.cos(process * Math.PI)) * 128);
                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        this.lightPixels.setPixelRGBA(j, i, gray << 16 | gray << 8 | gray);
                    }
                }
                this.lightTexture.upload();
                this.minecraft.getProfiler().pop();
                ci.cancel();
            }
        }
    }
}

package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(LightTexture.class)
public abstract class MixinLightTexture {
    @Final @Shadow private Minecraft minecraft;
    @Final @Shadow private DynamicTexture lightTexture;
    @Final @Shadow private NativeImage lightPixels;

    @Shadow private boolean updateLightTexture;
    @Shadow private float blockLightRedFlicker;
    @Shadow @Final private GameRenderer renderer;

    @Shadow private static void clampColor(Vector3f p_254122_) {}

    @Shadow public static float getBrightness(DimensionType p_234317_, int p_234318_) {
        return 0;
    }

    @Shadow protected abstract float getDarknessGamma(float p_234320_);

    @Shadow protected abstract float calculateDarknessScale(LivingEntity p_234313_, float p_234314_, float p_234315_);

    @Shadow protected abstract float notGamma(float p_109893_);

    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void getNightVisionScaleMix(float partialTick, CallbackInfo ci) {
        if (this.updateLightTexture) {
            ClientLevel clientlevel = this.minecraft.level;
            LocalPlayer player = this.minecraft.player;
            float useTick = player.getPersistentData().getFloat("AnniPowerTick");
            float lastTick = player.getPersistentData().getFloat("AnniLastTick");
            if (clientlevel != null && player != null) {
                if (SuperAddonHandler.isPunishedOne(player)) {
                    this.updateLightTexture = false;
                    this.minecraft.getProfiler().push("lightTex");
                    int punishment = SuperpositionHandler.getPersistentInteger(player, "Punishment", 0);
                    float process = Mth.clamp((float) punishment / 20.0F, 0.5F, 1.0F);
                    int gray = (int) ((1 + Math.cos(process * Math.PI)) * 128);
                    for (int i = 0; i < 16; ++i)
                        for (int j = 0; j < 16; ++j) {
                            this.lightPixels.setPixelRGBA(j, i, gray << 16 | gray << 8 | gray);
                        }
                    this.lightTexture.upload();
                    this.minecraft.getProfiler().pop();
                    ci.cancel();
                } else if (SuperpositionHandler.hasItem(player, EnigmaticAddonItems.ANNIHILATING_SWORD) && SuperpositionHandler.isTheWorthyOne(player) && useTick != 24.0F) {
                    float abyssModifier = 1.0F - useTick / 24.0F;
                    float lastModifier = 1.0F - lastTick / 24.0F;
                    abyssModifier = Mth.lerp(partialTick, lastModifier, abyssModifier);
                    float skyLight = clientlevel.getSkyDarken(1.0F);
                    float skyFlash;
                    if (clientlevel.getSkyFlashTime() > 0) skyFlash = 1.0F;
                    else skyFlash = skyLight * 0.95F + 0.05F;

                    float darkEffect = this.minecraft.options.darknessEffectScale().get().floatValue();
                    float dark = this.getDarknessGamma(partialTick) * darkEffect * (float) (Math.pow(2, abyssModifier) - 0.75) * 0.8F;
                    float darkModifier = this.calculateDarknessScale(this.minecraft.player, dark, partialTick) * darkEffect;
                    float waterModifier = this.minecraft.player.getWaterVision();
                    float brightModifier;
                    if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
                        brightModifier = GameRenderer.getNightVisionScale(this.minecraft.player, partialTick);
                    } else if (waterModifier > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
                        brightModifier = waterModifier;
                    } else brightModifier = 0.0F;

                    Vector3f skyVec = new Vector3f(skyLight, skyLight, 1.0F).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
                    float redFlicker = this.blockLightRedFlicker + 1.5F;
                    Vector3f colorVec = new Vector3f();

                    for (int i = 0; i < 16; ++i) {
                        for (int j = 0; j < 16; ++j) {
                            float flashModifier = getBrightness(clientlevel.dimensionType(), i) * skyFlash;
                            float rI = getBrightness(clientlevel.dimensionType(), j) * redFlicker;
                            float gI = rI * ((rI * 0.6F + 0.4F) * 0.6F + 0.4F);
                            float bI = rI * (rI * rI * 0.6F + 0.4F);
                            colorVec.set(rI, gI, bI);
                            boolean force = clientlevel.effects().forceBrightLightmap();
                            float tempValue;
                            Vector3f tempVec;
                            if (force) {
                                colorVec.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
                                clampColor(colorVec);
                            } else {
                                Vector3f vector3f2 = (new Vector3f(skyVec)).mul(flashModifier);
                                colorVec.add(vector3f2);
                                colorVec.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                                if (this.renderer.getDarkenWorldAmount(partialTick) > 0.0F) {
                                    tempValue = this.renderer.getDarkenWorldAmount(partialTick);
                                    tempVec = (new Vector3f(colorVec)).mul(0.7F, 0.6F, 0.6F);
                                    colorVec.lerp(tempVec, tempValue);
                                }
                            }

                            clientlevel.effects().adjustLightmapColors(clientlevel, partialTick, skyLight, redFlicker, flashModifier, j, i, colorVec);
                            colorVec.lerp(new Vector3f(0.443F, 0.294F, 0.439F), (float) (Math.pow(5, abyssModifier) - 1) * 0.25F);
                            float v;
                            if (brightModifier > 0.0F) {
                                v = Math.max(colorVec.x(), Math.max(colorVec.y(), colorVec.z()));
                                if (v < 1.0F) {
                                    tempValue = 1.0F / v;
                                    tempVec = (new Vector3f(colorVec)).mul(tempValue);
                                    colorVec.lerp(tempVec, brightModifier);
                                }
                            }

                            if (!force) {
                                if (darkModifier > 0.0F) colorVec.add(-darkModifier, -darkModifier, -darkModifier);
                                clampColor(colorVec);
                            }

                            Vector3f vector3f4 = new Vector3f(this.notGamma(colorVec.x), this.notGamma(colorVec.y), this.notGamma(colorVec.z));
                            colorVec.lerp(vector3f4, Math.max(0.0F, this.minecraft.options.gamma().get().floatValue() - dark));
                            colorVec.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                            clampColor(colorVec);
                            colorVec.lerp(new Vector3f(0, 0, 0), (float) (Math.pow(2, abyssModifier) - 1) * 0.5F);
                            colorVec.mul(255.0F);
                            int B = (int) colorVec.x();
                            int G = (int) colorVec.y();
                            int R = (int) colorVec.z();
                            this.lightPixels.setPixelRGBA(j, i, -16777216 | R << 16 | G << 8 | B);
                        }
                    }
                    this.lightTexture.upload();
                    this.minecraft.getProfiler().pop();
                    ci.cancel();
                }
            }
        }
    }
}

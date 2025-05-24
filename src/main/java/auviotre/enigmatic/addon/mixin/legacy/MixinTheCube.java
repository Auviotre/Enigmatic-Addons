package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.helpers.MixinOmniconfigHelper;
import com.aizistral.enigmaticlegacy.api.items.ISpellstone;
import com.aizistral.enigmaticlegacy.items.TheCube;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(TheCube.class)
public abstract class MixinTheCube implements ISpellstone {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"), remap = false)
    public ImmutableList<MobEffect> randomBuffs(Object e1, Object e2, Object e3, Object e4, Object e5, Object e6, Object e7, Object e8) {
        return enigmaticAddons$generator(MixinOmniconfigHelper.cubeRandomBuffs);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"), remap = false)
    public ImmutableList<MobEffect> randomDebuffs(Object e1, Object e2, Object e3, Object e4, Object e5, Object e6, Object e7, Object e8, Object e9) {
        return enigmaticAddons$generator(MixinOmniconfigHelper.cubeRandomDebuffs);
    }

    @Unique
    private ImmutableList<MobEffect> enigmaticAddons$generator(List<ResourceLocation> list) {
        ImmutableList.Builder<MobEffect> builder = ImmutableList.builder();
        for (ResourceLocation buff : list) {
            builder.add(ForgeRegistries.MOB_EFFECTS.getValue(buff));
        }
        return builder.build();
    }

    @Inject(method = "getDamageLimit(Z)F", at = @At("RETURN"), cancellable = true, remap = false)
    public void getDamageLimitMix(boolean cursed, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue((cursed ? 1.5F : 1.0F) * MixinOmniconfigHelper.cubeDamageLimit.getValue());
    }
}

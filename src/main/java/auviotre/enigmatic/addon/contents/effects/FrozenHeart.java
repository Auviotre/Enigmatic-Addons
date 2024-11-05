package auviotre.enigmatic.addon.contents.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class FrozenHeart extends MobEffect {
    public FrozenHeart() {
        super(MobEffectCategory.BENEFICIAL, 0xabfff1);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
    }
}

package auviotre.enigmatic.addon.contents.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class PureResistance extends MobEffect {
    public PureResistance() {
        super(MobEffectCategory.BENEFICIAL, 0xffbf4b);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
    }
}

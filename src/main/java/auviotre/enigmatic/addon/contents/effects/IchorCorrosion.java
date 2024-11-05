package auviotre.enigmatic.addon.contents.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class IchorCorrosion extends MobEffect {
    public IchorCorrosion() {
        super(MobEffectCategory.HARMFUL, 0xce753d);
        this.addAttributeModifier(Attributes.ARMOR, "56ea94eb-c5ff-43f6-b4a2-c98b3390b279", -0.1999995F, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "a5e421c9-d83a-48d4-a4c9-2f72848419ec", -0.1999995F, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "89fee955-c75e-4eeb-b4c2-80c4cc636a24", -0.1999995F, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}

package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.contents.effects.FrozenHeart;
import auviotre.enigmatic.addon.contents.effects.IchorCorrosion;
import auviotre.enigmatic.addon.contents.effects.PureResistance;
import auviotre.enigmatic.addon.contents.effects.RemainDragonBreath;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

public class EnigmaticAddonEffects extends AbstractRegistry<MobEffect> {
    private static final EnigmaticAddonEffects INSTANCE = new EnigmaticAddonEffects();
    @ObjectHolder(value = "enigmaticaddons:dragon_breath", registryName = "mob_effect")
    public static final RemainDragonBreath DRAGON_BREATH_EFFECT = null;

    @ObjectHolder(value = "enigmaticaddons:frozen_heart", registryName = "mob_effect")
    public static final FrozenHeart FROZEN_HEART_EFFECT = null;

    @ObjectHolder(value = "enigmaticaddons:pure_resistance", registryName = "mob_effect")
    public static final PureResistance PURE_RESISTANCE_EFFECT = null;

    @ObjectHolder(value = "enigmaticaddons:ichor_corrosion", registryName = "mob_effect")
    public static final IchorCorrosion ICHOR_CORROSION_EFFECT = null;

    private EnigmaticAddonEffects() {
        super(ForgeRegistries.MOB_EFFECTS);
        this.register("dragon_breath", RemainDragonBreath::new);
        this.register("frozen_heart", FrozenHeart::new);
        this.register("pure_resistance", PureResistance::new);
        this.register("ichor_corrosion", IchorCorrosion::new);
    }
}

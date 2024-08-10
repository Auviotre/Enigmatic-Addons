package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.EnigmaticAddons;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class EnigmaticAddonDamageTypes {
    public static final ResourceKey<DamageType> FALSE_JUSTICE = register("false_justice");

    private static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(EnigmaticAddons.MODID, name));
    }
}

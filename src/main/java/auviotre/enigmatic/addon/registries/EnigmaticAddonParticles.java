package auviotre.enigmatic.addon.registries;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

public class EnigmaticAddonParticles extends AbstractRegistry<ParticleType<?>> {
    @ObjectHolder(value = "enigmaticaddons:blue_star_dust", registryName = "particle_type")
    public static final SimpleParticleType BLUE_STAR_DUST = null;
    @ObjectHolder(value = "enigmaticaddons:red_star_dust", registryName = "particle_type")
    public static final SimpleParticleType RED_STAR_DUST = null;
    @ObjectHolder(value = "enigmaticaddons:purple_star_dust", registryName = "particle_type")
    public static final SimpleParticleType PURPLE_STAR_DUST = null;
    @ObjectHolder(value = "enigmaticaddons:ichor", registryName = "particle_type")
    public static final SimpleParticleType ICHOR = null;
    @ObjectHolder(value = "enigmaticaddons:abyss_chaos", registryName = "particle_type")
    public static final SimpleParticleType ABYSS_CHAOS = null;
    private static final EnigmaticAddonParticles INSTANCE = new EnigmaticAddonParticles();

    protected EnigmaticAddonParticles() {
        super(ForgeRegistries.PARTICLE_TYPES);
        this.register("blue_star_dust", () -> new SimpleParticleType(false));
        this.register("red_star_dust", () -> new SimpleParticleType(false));
        this.register("purple_star_dust", () -> new SimpleParticleType(false));
        this.register("ichor", () -> new SimpleParticleType(false));
        this.register("abyss_chaos", () -> new SimpleParticleType(false));
    }
}

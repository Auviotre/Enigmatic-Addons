package auviotre.enigmatic.addon.client.particles;

import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StarDustParticle extends SimpleAnimatedParticle {
    protected StarDustParticle(ClientLevel level, int color, double posX, double posY, double posZ, double dx, double dy, double dz, SpriteSet spriteSet) {
        super(level, posX, posY, posZ, spriteSet, 0.0125F);
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.quadSize *= 0.75F;
        this.friction = 0.775F;
        this.gravity = (float) (0.08F + Math.random() * 0.04F);
        this.lifetime = 30 + this.random.nextInt(20);
        this.setFadeColor(color);
        this.setSpriteFromAge(spriteSet);
    }

    public static ParticleOptions get(RandomSource random) {
        return switch (random.nextInt(3)) {
            case 0 -> EnigmaticAddonParticles.BLUE_STAR_DUST;
            case 1 -> EnigmaticAddonParticles.RED_STAR_DUST;
            default -> EnigmaticAddonParticles.PURPLE_STAR_DUST;
        };
    }

    public void tick() {
        this.gravity -= 0.012F;
        super.tick();
    }

    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
        this.setLocationFromBoundingbox();
    }

    @OnlyIn(Dist.CLIENT)
    public static class BlueProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public BlueProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double posX, double posY, double posZ, double speedX, double speedY, double speedZ) {
            return new StarDustParticle(level, 12841470, posX, posY, posZ, speedX, speedY, speedZ, this.sprites);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class RedProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public RedProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double posX, double posY, double posZ, double speedX, double speedY, double speedZ) {
            return new StarDustParticle(level, 16742489, posX, posY, posZ, speedX, speedY, speedZ, this.sprites);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class PurpleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public PurpleProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double posX, double posY, double posZ, double speedX, double speedY, double speedZ) {
            return new StarDustParticle(level, 16422123, posX, posY, posZ, speedX, speedY, speedZ, this.sprites);
        }
    }
}

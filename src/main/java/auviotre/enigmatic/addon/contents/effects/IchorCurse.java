package auviotre.enigmatic.addon.contents.effects;

import auviotre.enigmatic.addon.contents.entities.IchorSprite;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IchorCurse extends MobEffect {
    public IchorCurse() {
        super(MobEffectCategory.NEUTRAL, 0xFFBF4B);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(LivingEvent.@NotNull LivingTickEvent event) {
        if (event.getEntity() instanceof Monster monster && monster.level() instanceof ServerLevel server) {
            boolean infected = monster.getPersistentData().getBoolean("IchorInfected");
            if (infected && monster.tickCount % 2 == 0) {
                double hOffset = monster.getBbWidth() / 3;
                double yOffset = monster.getBbHeight() / 4;
                ParticleOptions particle = EnigmaticAddonParticles.ICHOR_CURSE;
                server.sendParticles(particle, monster.getX(), monster.getY(0.5), monster.getZ(), 1, hOffset, yOffset, hOffset, 0);
                monster.getAttributes().addTransientAttributeModifiers(ImmutableMultimap.of(Attributes.MAX_HEALTH, new AttributeModifier("ichor_curse_boost", 0.6, AttributeModifier.Operation.MULTIPLY_TOTAL)));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEffectApply(MobEffectEvent.@NotNull Applicable event) {
        if (event.getEntity() instanceof Player player) {
            if (!SuperAddonHandler.isOKOne(player) && event.getEffectInstance().getEffect().equals(EnigmaticAddonEffects.ICHOR_CURSE_EFFECT)) {
                event.setResult(MobEffectEvent.Applicable.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public void onEffectApply(MobEffectEvent.@NotNull Added event) {
        if (event.getEntity().hasEffect(EnigmaticAddonEffects.ICHOR_CURSE_EFFECT)) {
            MobEffectInstance instance = event.getEffectInstance();
            MobEffectInstance old = event.getOldEffectInstance();
            if (old != null && instance.getEffect().equals(EnigmaticAddonEffects.ICHOR_CURSE_EFFECT)) {
                if (instance.getDuration() < old.getDuration()) instance.duration = old.getDuration();
                int amplifier = instance.getAmplifier();
                instance.amplifier = Math.max(Math.min(4, 1 + amplifier + old.getAmplifier()), amplifier);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(@NotNull LivingDeathEvent event) {
        if (event.isCanceled()) return;
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (!attacker.hasEffect(EnigmaticAddonEffects.ICHOR_CURSE_EFFECT) || event.getEntity().getType().is(Tags.EntityTypes.BOSSES))
                return;
            int amplifier = Objects.requireNonNull(attacker.getEffect(EnigmaticAddonEffects.ICHOR_CURSE_EFFECT)).getAmplifier();
            if (event.getEntity() instanceof Monster monster && monster.getTarget() == attacker && monster.level() instanceof ServerLevel level) {
                if (!monster.level().dimension().equals(Level.NETHER)) return;
                if (monster.getSpawnType() == null || !monster.getSpawnType().equals(MobSpawnType.NATURAL)) return;
                boolean infected = monster.getPersistentData().getBoolean("IchorInfected");
                if (!infected && monster.hasLineOfSight(attacker)) {
                    monster.getPersistentData().putBoolean("IchorInfected", true);
                    for (int i = 0; i < monster.getRandom().nextInt(1, 3 + amplifier / 2); i++) {
                        IchorSprite sprite = EnigmaticAddonEntities.ICHOR_SPRITE.create(level);
                        if (sprite != null) {
                            sprite.setPos(monster.getEyePosition());
                            sprite.setOwner(monster);
                            level.addFreshEntity(sprite);
                            level.sendParticles(ParticleTypes.EXPLOSION, sprite.getX(), sprite.getY(), sprite.getZ(), 1, 0, 0, 0, 0);
                        }
                    }
                    event.setCanceled(true);
                    monster.setHealth(monster.getMaxHealth() * (0.75F + 0.05F * amplifier));
                    attacker.knockback(0.2F, Mth.sin((float) Math.toRadians(monster.getYRot())), -Mth.cos((float) Math.toRadians(monster.getYRot())));
                    monster.addEffect(new MobEffectInstance(EnigmaticAddonEffects.PURE_RESISTANCE_EFFECT, 200, 3));
                }
            }
        }
    }
}

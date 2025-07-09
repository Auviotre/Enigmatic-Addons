package auviotre.enigmatic.addon.triggers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class ChaosElytraKillTrigger extends SimpleCriterionTrigger<ChaosElytraKillTrigger.Instance> {
    public static final ResourceLocation ID = new ResourceLocation(EnigmaticAddons.MODID, "chaos_explosion_kill");
    public static final ChaosElytraKillTrigger INSTANCE = new ChaosElytraKillTrigger();

    @Nonnull
    public ResourceLocation getId() {
        return ID;
    }

    @Nonnull
    public Instance createInstance(@Nonnull JsonObject json, @Nonnull ContextAwarePredicate player, DeserializationContext conditions) {
        return new ChaosElytraKillTrigger.Instance(player);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Instance::test);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        Instance(ContextAwarePredicate player) {
            super(ChaosElytraKillTrigger.ID, player);
        }

        @Nonnull
        public ResourceLocation getCriterion() {
            return ChaosElytraKillTrigger.ID;
        }

        boolean test() {
            return true;
        }
    }
}

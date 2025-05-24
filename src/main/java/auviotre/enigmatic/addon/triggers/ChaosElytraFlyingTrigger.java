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

public class ChaosElytraFlyingTrigger extends SimpleCriterionTrigger<ChaosElytraFlyingTrigger.Instance> {
    public static final ResourceLocation ID = new ResourceLocation(EnigmaticAddons.MODID, "using_chaos_elytra");
    public static final ChaosElytraFlyingTrigger INSTANCE = new ChaosElytraFlyingTrigger();

    @Nonnull
    public ResourceLocation getId() {
        return ID;
    }

    @Nonnull
    public Instance createInstance(@Nonnull JsonObject json, @Nonnull ContextAwarePredicate player, DeserializationContext conditions) {
        return new ChaosElytraFlyingTrigger.Instance(player);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Instance::test);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        Instance(ContextAwarePredicate player) {
            super(ChaosElytraFlyingTrigger.ID, player);
        }

        @Nonnull
        public ResourceLocation getCriterion() {
            return ChaosElytraFlyingTrigger.ID;
        }

        boolean test() {
            return true;
        }
    }
}

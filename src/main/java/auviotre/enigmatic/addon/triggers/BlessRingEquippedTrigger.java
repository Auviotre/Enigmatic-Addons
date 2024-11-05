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

public class BlessRingEquippedTrigger extends SimpleCriterionTrigger<BlessRingEquippedTrigger.Instance> {
    public static final ResourceLocation ID = new ResourceLocation(EnigmaticAddons.MODID, "equip_bless_ring");
    public static final BlessRingEquippedTrigger INSTANCE = new BlessRingEquippedTrigger();

    @Nonnull
    public ResourceLocation getId() {
        return ID;
    }

    @Nonnull
    public BlessRingEquippedTrigger.Instance createInstance(@Nonnull JsonObject json, @Nonnull ContextAwarePredicate player, DeserializationContext conditions) {
        return new BlessRingEquippedTrigger.Instance(player);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Instance::test);
    }

    static class Instance extends AbstractCriterionTriggerInstance {
        Instance(ContextAwarePredicate player) {
            super(BlessRingEquippedTrigger.ID, player);
        }

        @Nonnull
        public ResourceLocation getCriterion() {
            return BlessRingEquippedTrigger.ID;
        }

        boolean test() {
            return true;
        }
    }
}

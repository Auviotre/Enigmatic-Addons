package auviotre.enigmatic.addon.mixin.entity;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class MixinVillager implements ReputationEventHandler {
    @Shadow @Final private GossipContainer gossips;

    @Inject(method = "onReputationEventFrom", at = @At("TAIL"))
    public void onReputationEventFromMix(ReputationEventType type, Entity entity, CallbackInfo ci) {
        if (entity instanceof Player player && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.AVARICE_RING)) {
            for (GossipType gossipType : GossipType.values()) {
                if (gossipType.weight < 0 && this.gossips.getReputation(player.getUUID(), gossip -> gossip.equals(gossipType)) < 0) {
                    this.gossips.remove(gossipType);
                }
            }
        }
    }
}

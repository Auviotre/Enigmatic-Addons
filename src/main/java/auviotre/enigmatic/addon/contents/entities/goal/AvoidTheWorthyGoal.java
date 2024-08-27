package auviotre.enigmatic.addon.contents.entities.goal;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;

public class AvoidTheWorthyGoal extends AvoidEntityGoal<Player> {
    public AvoidTheWorthyGoal(PathfinderMob mob, float maxDist, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, Player.class, maxDist, walkSpeedModifier, sprintSpeedModifier, (player) -> SuperpositionHandler.isTheWorthyOne((Player) player) && !SuperpositionHandler.hasItem((Player) player, EnigmaticAddonItems.LIVING_ODE));
    }

    public boolean canUse() {
        return SuperAddonHandler.isCurseBoosted(this.mob) && super.canUse();
    }
}

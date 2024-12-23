package auviotre.enigmatic.addon.contents.entities.goal;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

public class AvoidTheWorthyGoal extends AvoidEntityGoal<Player> {
    public AvoidTheWorthyGoal(Animal mob, float maxDist, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, Player.class, maxDist, walkSpeedModifier, sprintSpeedModifier, (player) -> {
            boolean cursePlayer = SuperpositionHandler.isTheWorthyOne((Player) player) && !SuperpositionHandler.hasItem((Player) player, EnigmaticAddonItems.LIVING_ODE);
            boolean notTarget = mob.getTarget() == null || mob.getTarget() != player;
            return mob.getLoveCause() == null && cursePlayer && notTarget && !(mob instanceof NeutralMob);
        });
    }

    public boolean canUse() {
        return SuperAddonHandler.isCurseBoosted(this.mob) && super.canUse();
    }
}

package auviotre.enigmatic.addon.api.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;

public class LivingCurseBoostEvent extends LivingEvent {
    /**
     * This Event is fired when a {@link LivingEntity} interacts with a player
     * which has worn the Ring of Seven Curses for enough time. (the Worthy One)
     **/

    private final @Nullable Player worthyCursed;

    public LivingCurseBoostEvent(LivingEntity entity, Player player) {
        super(entity);
        this.worthyCursed = player;
    }

    public Player getTheWorthyOne() {
        return this.worthyCursed;
    }
}

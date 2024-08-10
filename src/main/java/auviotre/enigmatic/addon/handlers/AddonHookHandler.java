package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.api.events.LivingCurseBoostEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class AddonHookHandler {
    public static LivingCurseBoostEvent onLivingCurseBoosted(LivingEntity entity, @Nullable Player worthyOne) {
        LivingCurseBoostEvent event = new LivingCurseBoostEvent(entity, worthyOne);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
}

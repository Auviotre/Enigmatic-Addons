package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.api.events.LivingCurseBoostEvent;
import auviotre.enigmatic.addon.api.events.RenderHandEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

public class AddonHookHandler {
    public static LivingCurseBoostEvent onLivingCurseBoosted(LivingEntity entity, @Nullable Player worthyOne) {
        LivingCurseBoostEvent event = new LivingCurseBoostEvent(entity, worthyOne);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    @ApiStatus.Internal
    public static boolean renderSpecificFirstPersonHand(InteractionHand hand, LocalPlayer player, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick, float interpPitch, float swingProgress, float equipProgress, ItemStack stack) {
        return MinecraftForge.EVENT_BUS.post(new RenderHandEvent(hand, player, poseStack, bufferSource, packedLight, partialTick, interpPitch, swingProgress, equipProgress, stack));
    }
}

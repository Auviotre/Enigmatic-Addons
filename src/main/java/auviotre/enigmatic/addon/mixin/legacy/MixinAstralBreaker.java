package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.packets.clients.PacketStarParticles;
import com.aizistral.enigmaticlegacy.items.AstralBreaker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(AstralBreaker.class)
public class MixinAstralBreaker {
    /**
     * @author Auviotre
     * @reason Replace the particle effect.
     */
    @Overwrite(remap = false)
    public void spawnFlameParticles(Level world, BlockPos pos) {
        Vec3 center = pos.getCenter();
        EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 128.0, world.dimension())),
                new PacketStarParticles(center.x, center.y, center.z, 18, 1));
    }
}

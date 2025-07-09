package auviotre.enigmatic.addon.packets.clients;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketForgottenIce {
    private final double x;
    private final double y;
    private final double z;

    public PacketForgottenIce(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(PacketForgottenIce msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    public static PacketForgottenIce decode(FriendlyByteBuf buf) {
        return new PacketForgottenIce(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(PacketForgottenIce msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft instance = Minecraft.getInstance();
            Player player = EnigmaticLegacy.PROXY.getClientPlayer();
            ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Blocks.BLUE_ICE));
            instance.particleEngine.createTrackingEmitter(player, ParticleTypes.SNOWFLAKE, 10);
            instance.particleEngine.createTrackingEmitter(player, particle, 10);
            instance.level.playLocalSound(msg.x, msg.y, msg.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6F, 2.0F, true);
        });
        context.get().setPacketHandled(true);
    }
}
package auviotre.enigmatic.addon.packets;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PacketCursedXPScrollKey {
    private final boolean pressed;

    public PacketCursedXPScrollKey(boolean pressed) {
        this.pressed = pressed;
    }

    public static void encode(PacketCursedXPScrollKey msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.pressed);
    }

    public static @NotNull PacketCursedXPScrollKey decode(FriendlyByteBuf buf) {
        return new PacketCursedXPScrollKey(buf.readBoolean());
    }

    public static void handle(PacketCursedXPScrollKey msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer playerServ = ctx.get().getSender();
            if (SuperpositionHandler.hasCurio(playerServ, EnigmaticAddonItems.CURSED_XP_SCROLL)) {
                ItemStack scroll = SuperpositionHandler.getCurioStack(playerServ, EnigmaticAddonItems.CURSED_XP_SCROLL);
                EnigmaticAddonItems.CURSED_XP_SCROLL.trigger(playerServ.level(), scroll, playerServ, InteractionHand.MAIN_HAND, false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

package auviotre.enigmatic.addon.packets.server;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Supplier;

public class PacketEmptyLeftClick {
    private final boolean clicked;

    public PacketEmptyLeftClick(boolean clicked) {
        this.clicked = clicked;
    }

    public static void encode(PacketEmptyLeftClick msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.clicked);
    }

    public static @NotNull PacketEmptyLeftClick decode(FriendlyByteBuf buf) {
        return new PacketEmptyLeftClick(buf.readBoolean());
    }

    public static void handle(PacketEmptyLeftClick msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer servPlayer = context.get().getSender();
            float base = (float) (0.5F * servPlayer.getAttributeValue(Attributes.ATTACK_DAMAGE));
            float damage = EnchantmentHelper.getSweepingDamageRatio(servPlayer) * base;
            double delX = -Mth.sin(servPlayer.getYRot() * 0.017453292F);
            double delY = Mth.cos(servPlayer.getYRot() * 0.017453292F);
            AABB sweepHitBox = servPlayer.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(servPlayer, servPlayer);
            Iterator<LivingEntity> iterator = servPlayer.level().getEntitiesOfClass(LivingEntity.class, sweepHitBox.move(delX, 0, delY)).iterator();

            loop:
            while (true) {
                LivingEntity livingentity;
                double entityReachSq;
                do {
                    do {
                        do {
                            if (!iterator.hasNext()) {
                                servPlayer.level().playSound(null, servPlayer.getX(), servPlayer.getY(), servPlayer.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, servPlayer.getSoundSource(), 1.0F, 1.0F);
                                servPlayer.sweepAttack();
                                break loop;
                            }

                            livingentity = iterator.next();
                            entityReachSq = Mth.square(servPlayer.getEntityReach());
                        } while (livingentity == servPlayer);
                    } while (servPlayer.isAlliedTo(livingentity));
                } while (livingentity instanceof ArmorStand && ((ArmorStand) livingentity).isMarker());

                if (servPlayer.distanceToSqr(livingentity) < entityReachSq) {
                    livingentity.knockback(0.4, -delX, -delY);
                    livingentity.hurt(servPlayer.damageSources().playerAttack(servPlayer), damage);
                }
            }
            servPlayer.causeFoodExhaustion(0.1F);
            servPlayer.getCooldowns().addCooldown(EnigmaticAddonItems.THUNDER_SCROLL, (int) (16 / servPlayer.getAttributeValue(Attributes.ATTACK_SPEED)));
        });
        context.get().setPacketHandled(true);
    }
}

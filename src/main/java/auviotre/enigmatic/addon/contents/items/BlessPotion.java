package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBasePotion;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPortalParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class BlessPotion extends ItemBasePotion implements ICursed {
    public BlessPotion() {
        super(getDefaultProperties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessPotion1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessPotion2");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }


    public void onConsumed(Level world, Player player, ItemStack potion) {
        CompoundTag location = this.getLastDeathLocation(player);
        if (location != null) {
            double x = location.getDouble("x");
            double y = location.getDouble("y");
            double z = location.getDouble("z");
            if (player instanceof ServerPlayer serverPlayer) {
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(location.getString("dimension")));
                serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8 + Math.random() * 0.2));
                EnigmaticLegacy.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 128.0, serverPlayer.level().dimension())), new PacketPortalParticles(serverPlayer.getX(), serverPlayer.getY() + (double) (serverPlayer.getBbHeight() / 2.0F), serverPlayer.getZ(), 100, 1.25, false));
                SuperpositionHandler.sendToDimension(serverPlayer, dimension);
                player.moveTo(x, y, z);
                serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8 + Math.random() * 0.2));
                EnigmaticLegacy.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 128.0, serverPlayer.level().dimension())), new PacketRecallParticles(serverPlayer.getX(), serverPlayer.getY() + (double) (serverPlayer.getBbHeight() / 2.0F), serverPlayer.getZ(), 48, false));
            }
        }
        player.addEffect(new MobEffectInstance(EnigmaticAddonEffects.PURE_RESISTANCE_EFFECT, 100, 2));
    }

    public boolean canDrink(Level world, Player player, ItemStack potion) {
        return SuperpositionHandler.isTheCursedOne(player) && this.getLastDeathLocation(player) != null;
    }

    private CompoundTag getLastDeathLocation(Player player) {
        return (CompoundTag) SuperpositionHandler.getPersistentTag(player, "LastDeathLocation", null);
    }
}

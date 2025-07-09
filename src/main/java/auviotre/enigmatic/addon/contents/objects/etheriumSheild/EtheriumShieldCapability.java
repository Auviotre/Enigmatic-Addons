package auviotre.enigmatic.addon.contents.objects.etheriumSheild;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.packets.server.PacketEtheriumShieldSync;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EtheriumShieldCapability {
    public static final Capability<IEtheriumShieldData> ETHERIUM_SHIELD_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation ID_ETHERIUM_SHIELD_DATA = new ResourceLocation(EnigmaticAddons.MODID, "etherium_shield_data");

    public EtheriumShieldCapability() {
    }

    public static ICapabilityProvider createProvider() {
        return new Provider();
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        final LazyOptional<IEtheriumShieldData> optional;
        final IEtheriumShieldData handler;

        Provider() {
            this.handler = new Wrapper();
            this.optional = LazyOptional.of(() -> this.handler);
        }

        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {
            return EtheriumShieldCapability.ETHERIUM_SHIELD_DATA.orEmpty(capability, this.optional);
        }

        public CompoundTag serializeNBT() {
            return this.handler.writeTag();
        }

        public void deserializeNBT(CompoundTag nbt) {
            this.handler.readTag(nbt);
        }
    }

    public static class Wrapper implements IEtheriumShieldData {
        int shieldTick = 0;
        boolean init = true;

        public Wrapper() {
        }

        public int getTick() {
            return this.shieldTick;
        }

        public void setTick(int tick) {
            this.shieldTick = tick;
        }

        public void reset() {
            this.shieldTick = 0;
        }

        public void tick(Player player) {
            tick(player, 1);
        }

        public void tick(Player player, int count) {
            if (this.init) this.init = false;
            else if (player.isAlive()) this.shieldTick = Math.max(0, this.shieldTick - count);
            this.update((ServerPlayer) player);
        }

        private void update(ServerPlayer player) {
            EnigmaticAddons.packetInstance.send(PacketDistributor.PLAYER.with(() -> player), new PacketEtheriumShieldSync(this.shieldTick));
        }

        public CompoundTag writeTag() {
            CompoundTag compound = new CompoundTag();
            compound.putInt("EtheriumShieldTick", this.shieldTick);
            return compound;
        }

        public void readTag(CompoundTag nbt) {
            this.shieldTick = nbt.getInt("EtheriumShieldTick");
        }
    }
}

package auviotre.enigmatic.addon.contents.objects.etheriumSheild;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface IEtheriumShieldData {
    void tick(Player player, int count);

    void tick(Player player);

    int getTick();

    void setTick(int tick);

    void reset();

    CompoundTag writeTag();

    void readTag(CompoundTag tag);
}

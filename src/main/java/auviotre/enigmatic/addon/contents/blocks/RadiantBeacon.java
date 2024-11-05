package auviotre.enigmatic.addon.contents.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class RadiantBeacon extends Block {
    public RadiantBeacon() {
        super(Properties.copy(Blocks.BEACON).lightLevel((state) -> 15));
    }
}

package auviotre.enigmatic.addon.proxy;

import auviotre.enigmatic.addon.contents.items.ArtificialFlower;
import auviotre.enigmatic.addon.contents.items.IchorSpear;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import net.minecraft.world.level.block.DispenserBlock;

public class CommonProxy {
    public CommonProxy() {
    }

    public void clientInit() {
    }

    public void commonInit() {
        DispenserBlock.registerBehavior(EnigmaticAddonItems.ICHOR_SPEAR, IchorSpear.DISPENSE_BEHAVIOR);
        ArtificialFlower.Helper.initRandomPool();
    }
}

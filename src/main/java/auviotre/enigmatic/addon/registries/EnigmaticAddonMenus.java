package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.contents.gui.AntiqueBagContainerMenu;
import auviotre.enigmatic.addon.contents.gui.ArtificialFlowerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

public class EnigmaticAddonMenus extends AbstractRegistry<MenuType<?>> {
    @ObjectHolder(value = "enigmaticaddons:antique_bag_menu", registryName = "menu")
    public static final MenuType<AntiqueBagContainerMenu> ANTIQUE_BAG_MENU = null;
    @ObjectHolder(value = "enigmaticaddons:artificial_flower_menu", registryName = "menu")
    public static final MenuType<ArtificialFlowerMenu> ARTIFICIAL_FLOWER_MENU = null;
    private static final EnigmaticAddonMenus INSTANCE = new EnigmaticAddonMenus();

    private EnigmaticAddonMenus() {
        super(ForgeRegistries.MENU_TYPES);
        this.register("antique_bag_menu", () -> IForgeMenuType.create(AntiqueBagContainerMenu::new));
        this.register("artificial_flower_menu", () -> IForgeMenuType.create(ArtificialFlowerMenu::new));
    }

}

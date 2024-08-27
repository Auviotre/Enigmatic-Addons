package auviotre.enigmatic.addon.client.screens;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.gui.AntiqueBagContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class AntiqueBagScreen extends AbstractContainerScreen<AntiqueBagContainerMenu> {

    public static final ResourceLocation BAG_INVENTORY = new ResourceLocation(EnigmaticAddons.MODID, "textures/gui/antique_bag_gui.png");

    public AntiqueBagScreen(AntiqueBagContainerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    protected void init() {
        super.init();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BAG_INVENTORY, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
}

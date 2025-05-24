package auviotre.enigmatic.addon.client.screens;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.gui.ArtificialFlowerMenu;
import auviotre.enigmatic.addon.contents.items.ArtificialFlower;
import auviotre.enigmatic.addon.handlers.CompatHandler;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class ArtificialFlowerScreen extends AbstractContainerScreen<ArtificialFlowerMenu> {
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), (format) -> format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
    public static final ResourceLocation INVENTORY = new ResourceLocation(EnigmaticAddons.MODID, "textures/gui/artificial_flower_gui.png");
    private final MobEffect[] effectsLegacy = {null, null};
    private final TextureAtlasSprite[] effectSpritesLegacy = {null, null};

    public ArtificialFlowerScreen(ArtificialFlowerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    public static Component getComponent(Pair<Attribute, AttributeModifier> attribute) {
        MutableComponent name = Component.translatable(attribute.getFirst().getDescriptionId());
        AttributeModifier attributemodifier = attribute.getSecond();
        double amount = attributemodifier.getAmount();
        if (attributemodifier.getOperation() != AttributeModifier.Operation.ADDITION) amount = amount * 100.0;
        if (amount > 0.0) {
            return Component.translatable("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(amount), name).withStyle(ChatFormatting.GREEN);
        } else if (amount < 0.0) {
            amount *= -1.0;
            return Component.translatable("attribute.modifier.take." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(amount), name).withStyle(ChatFormatting.RED);
        }
        return Component.empty();
    }

    protected void init() {
        super.init();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int clickType) {
        int x0 = (this.width - this.imageWidth) / 2;
        int y0 = (this.height - this.imageHeight) / 2;
        for (int id = 0; id < 3; id++) {
            double dx = mouseX - (x0 + 53);
            double dy = mouseY - (y0 + 14 + 20 * id);
            if (dx >= 0.0 && dy >= 0.0 && dx < 10.0 && dy < 10.0 && this.menu.clickMenuButton(this.minecraft.player, id)) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
                return true;
            }
        }
        if (x0 + 16.0 <= mouseX && mouseX <= 33.0 + x0 && y0 + 49.0 <= mouseY && mouseY <= 55.0 + y0 && this.menu.clickMenuButton(this.minecraft.player, 3)) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 3);
            return true;
        }
        for (int id = 4; id < 6; id++) {
            double dx = mouseX - (x0 + 139);
            double dy = mouseY - (y0 + 16 + 26 * (id - 4));
            if (dx >= 0.0 && dy >= 0.0 && dx < 20.0 && dy < 20.0 && this.menu.clickMenuButton(this.minecraft.player, id)) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, clickType);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        int x0 = (this.width - this.imageWidth) / 2;
        int y0 = (this.height - this.imageHeight) / 2;
        ItemStack flower = ArtificialFlower.Helper.getFlowerStack(this.menu.player, false);
        for (int id = 0; id < 3; ++id) {
            int dx = mouseX - (x0 + 53);
            int dy = mouseY - (y0 + 14 + 20 * id);
            if (dx >= 0.0 && dy >= 0.0 && dx < 10.0 && dy < 10.0) {
                Pair<Attribute, AttributeModifier> attribute = ArtificialFlower.Helper.getAttribute(flower, id + 1);
                if (attribute != null) {
                    graphics.blit(INVENTORY, x0 + 54, y0 + id * 20 + 15, 176, 8, 8, 8);
                    List<Component> components = new ArrayList<>();
                    components.add(getComponent(attribute));
                    if (ModList.get().isLoaded("jade") && attribute != null) {
                        (new CompatHandler()).addModNameFromJade(components, ForgeRegistries.ATTRIBUTES.getKey(attribute.getFirst()));
                    }
                    graphics.renderComponentTooltip(this.font, components, mouseX, mouseY + 5);
                } else {
                    graphics.blit(INVENTORY, x0 + 54, y0 + id * 20 + 15, 176, 16, 8, 8);
                }
            }
        }
        if (x0 + 16.0 <= mouseX && mouseX <= 33.0 + x0 && y0 + 49.0 <= mouseY && mouseY <= 55.0 + y0) {
            int cost = this.menu.costMode == 0 ? 2 : this.menu.costMode == 1 ? 4 : 8;
            graphics.renderTooltip(this.font, Component.translatable("gui.enigmaticaddons.artificial_flower_cost", cost).withStyle(ChatFormatting.GOLD), mouseX, mouseY + 5);
        }
        for (int id = 0; id < 2; id++) {
            double dx = mouseX - (x0 + 139);
            double dy = mouseY - (y0 + 16 + 26 * id);
            if (dx >= 0.0 && dy >= 0.0 && dx < 20.0 && dy < 20.0) {
                Component name;
                int suffix;
                MobEffect effect = ArtificialFlower.Helper.getEffect(flower, id);
                if (effect == null) {
                    suffix = 0;
                    name = Component.translatable("tooltip.enigmaticaddons.artificialFlowerNone");
                } else {
                    suffix = this.menu.hasRing() ? 1 : 0;
                    name = Component.translatable(effect.getDescriptionId()).withStyle(effect.isBeneficial() ? ChatFormatting.GREEN : ChatFormatting.RED);
                }
                Component immunity = Component.translatable("gui.enigmaticaddons.artificial_flower_immunity", name);
                Component providing = Component.translatable("gui.enigmaticaddons.artificial_flower_provide" + suffix, name);
                List<Component> components = new ArrayList<>();
                components.add(id == 0 ? providing : immunity);

                if (ModList.get().isLoaded("jade") && effect != null) {
                    (new CompatHandler()).addModNameFromJade(components, ForgeRegistries.MOB_EFFECTS.getKey(effect));
                }
                graphics.renderComponentTooltip(this.font, components, mouseX, mouseY + 5);
            }
        }
    }

    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x0 = (this.width - this.imageWidth) / 2;
        int y0 = (this.height - this.imageHeight) / 2;
        graphics.blit(INVENTORY, x0, y0, 0, 0, this.imageWidth, this.imageHeight);
        graphics.drawString(this.font, Component.translatable("gui.enigmaticaddons.artificial_flower_attribute").withStyle(ChatFormatting.WHITE), x0 + 10, y0 + 5, 10);
        graphics.drawString(this.font, Component.translatable("gui.enigmaticaddons.artificial_flower_effect").withStyle(ChatFormatting.WHITE), x0 + 100, y0 + 5, 10);
        if (this.menu.valid(0)) graphics.blit(INVENTORY, x0 + 36, y0 + 15, 192, 0, 16, 48);
        if (this.menu.valid(1)) {
            graphics.blit(INVENTORY, x0 + 112, y0 + 22, 176, 48, 5 + 20, 8);
            graphics.blit(INVENTORY, x0 + 112, y0 + 49, 176, 56, 5 + 20, 8);
        }

        graphics.blit(INVENTORY, x0 + 17 + this.menu.costMode * 6, y0 + 49, 176, 32, 4, 7);
        ItemStack flower = ArtificialFlower.Helper.getFlowerStack(this.menu.player, true);
        int dy = 15;
        for (int id = 1; id <= 3; ++id) {
            if (ArtificialFlower.Helper.getAttribute(flower, id) != null) {
                graphics.blit(INVENTORY, x0 + 54, y0 + dy, 176, 0, 8, 8);
            }
            dy += 20;
        }
        dy = 17;
        for (int id = 0; id < 2; ++id) {
            if (ArtificialFlower.Helper.getEffect(flower, id) != null) {
                if (effectsLegacy[id] == null || (effectsLegacy[id] != ArtificialFlower.Helper.getEffect(flower, id))) {
                    effectsLegacy[id] = ArtificialFlower.Helper.getEffect(flower, id);
                    effectSpritesLegacy[id] = Minecraft.getInstance().getMobEffectTextures().get(effectsLegacy[id]);
                }
                graphics.blit(x0 + 140, y0 + dy, 0, 18, 18, effectSpritesLegacy[id]);
            }
            dy += 26;
        }
    }
}

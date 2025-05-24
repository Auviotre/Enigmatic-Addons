package auviotre.enigmatic.addon.handlers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class CompatHandler {
    public void addModNameFromJade(List<Component> components, ResourceLocation location) {
        components.add(Component.literal(snownee.jade.util.ModIdentification.getModName(location)).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
    }
}

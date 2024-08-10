package auviotre.enigmatic.addon.helpers;

import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MixinOmniconfigHelper {
    private static final String category = "Legacy Balance Options";
    public static final List<ResourceLocation> cubeRandomBuffs = new ArrayList<>();
    private static final String[] defaultRandomBuffs = new String[]{
            "minecraft:absorption", "minecraft:haste", "minecraft:jump_boost", "minecraft:regeneration", "minecraft:resistance", "minecraft:speed", "minecraft:strength", "minecraft:slow_falling"
    };
    public static final List<ResourceLocation> cubeRandomDebuffs = new ArrayList<>();
    private static final String[] defaultRandomDebuffs = new String[]{
            "minecraft:blindness", "minecraft:nausea", "minecraft:mining_fatigue", "minecraft:hunger", "minecraft:levitation", "minecraft:slowness", "minecraft:weakness", "minecraft:poison", "minecraft:wither"
    };

    public static void MixConfig(OmniconfigWrapper builder) {
        cubeRandomBuffs.clear();
        String[] buffList = builder.config.getStringList("TheCubeRandomBuffs", category, defaultRandomBuffs, "List of effects that will appear in The Cube's random buffs. Examples: minecraft:absorption, minecraft:strength. Changing this option required game restart to take effect.");
        Arrays.stream(buffList).forEach((entry) -> {
            cubeRandomBuffs.add(new ResourceLocation(entry));
        });
        if (cubeRandomBuffs.isEmpty()) cubeRandomBuffs.add(new ResourceLocation("minecraft:strength"));
        cubeRandomDebuffs.clear();
        String[] debuffList = builder.config.getStringList("TheCubeRandomDebuffs", category, defaultRandomDebuffs, "List of effects that will appear in The Cube's random debuffs. Examples: minecraft:blindness, minecraft:nausea. Changing this option required game restart to take effect.");
        Arrays.stream(debuffList).forEach((entry) -> {
            cubeRandomDebuffs.add(new ResourceLocation(entry));
        });
        if (cubeRandomDebuffs.isEmpty()) cubeRandomDebuffs.add(new ResourceLocation("minecraft:weakness"));
    }
}

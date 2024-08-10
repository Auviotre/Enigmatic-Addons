package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.contents.SpecialLootModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Supplier;

public class EnigmaticAddonLootModifier extends AbstractRegistry<Codec<? extends IGlobalLootModifier>> {
    private static final EnigmaticAddonLootModifier INSTANCE = new EnigmaticAddonLootModifier();

    private EnigmaticAddonLootModifier() {
        super(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS);
        Supplier<Codec<SpecialLootModifier>> codec = SpecialLootModifier.CODEC;
        Objects.requireNonNull(codec);
        this.register("special_loot_modifier", codec::get);
    }
}

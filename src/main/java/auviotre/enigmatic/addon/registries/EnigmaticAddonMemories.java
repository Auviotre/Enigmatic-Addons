package auviotre.enigmatic.addon.registries;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Optional;

public class EnigmaticAddonMemories extends AbstractRegistry<MemoryModuleType<?>>{
    @ObjectHolder(value = "enigmaticaddons:ichor_sprite_owner", registryName = "memory_module_type")
    public static final MemoryModuleType<LivingEntity> ICHOR_SPRITE_OWNER = null;
    private static final EnigmaticAddonMemories INSTANCE = new EnigmaticAddonMemories();

    protected EnigmaticAddonMemories() {
        super(ForgeRegistries.MEMORY_MODULE_TYPES);
        this.register("ichor_sprite_owner", () -> new MemoryModuleType<>(Optional.empty()));
    }
}

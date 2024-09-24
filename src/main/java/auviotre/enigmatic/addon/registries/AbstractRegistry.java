package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.EnigmaticAddons;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AbstractRegistry<T> {
    private final DeferredRegister<T> register;

    protected AbstractRegistry(ResourceKey<Registry<T>> registry, String modid) {
        this.register = DeferredRegister.create(registry, modid);
        this.register.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterEvent);
    }

    protected AbstractRegistry(IForgeRegistry<T> registry, String modid) {
        this(registry.getRegistryKey(), modid);
    }

    protected AbstractRegistry(ResourceKey<Registry<T>> registry) {
        this(registry, EnigmaticAddons.MODID);
    }

    protected AbstractRegistry(IForgeRegistry<T> registry) {
        this(registry, EnigmaticAddons.MODID);
    }

    protected void register(String name, Supplier<T> supplier) {
        this.register.register(name, supplier);
    }

    private void onRegisterEvent(RegisterEvent event) {
        if (event.getRegistryKey() == this.register.getRegistryKey()) {
            this.onRegister(event);
        }
    }

    protected void onRegister(RegisterEvent event) {
    }

    public static void loadClass(@NotNull Class<?> theClass) {
        try {
            Class.forName(theClass.getName());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("This can't be happening.");
        }
    }
}


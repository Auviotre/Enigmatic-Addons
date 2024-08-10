package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.EnigmaticAddons;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.Supplier;

public abstract class AbstractRegistry<T> {
    private final DeferredRegister<T> register;

    protected AbstractRegistry(ResourceKey<Registry<T>> registry) {
        this.register = DeferredRegister.create(registry, EnigmaticAddons.MODID);
        this.register.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterEvent);
    }

    protected AbstractRegistry(IForgeRegistry<T> registry) {
        this(registry.getRegistryKey());
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
}


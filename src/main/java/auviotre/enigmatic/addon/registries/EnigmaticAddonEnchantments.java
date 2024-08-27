package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.contents.enchantments.FrostAspectEnchantment;
import auviotre.enigmatic.addon.contents.enchantments.FrostProtectionEnchantment;
import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

public class EnigmaticAddonEnchantments extends AbstractRegistry<Enchantment> {
    private static final EnigmaticAddonEnchantments INSTANCE = new EnigmaticAddonEnchantments();
    @ConfigurableItem("Frost Protection Enchantment")
    @ObjectHolder(
            value = "enigmaticaddons:frost_protection",
            registryName = "enchantment"
    )
    public static final FrostProtectionEnchantment FROST_PROTECTION = null;
    @ConfigurableItem("Frost Aspect Enchantment")
    @ObjectHolder(
            value = "enigmaticaddons:frost_aspect",
            registryName = "enchantment"
    )
    public static final FrostAspectEnchantment FROST_ASPECT = null;

    private EnigmaticAddonEnchantments() {
        super(ForgeRegistries.ENCHANTMENTS);
        EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        this.register("frost_aspect", () -> new FrostAspectEnchantment(Enchantment.Rarity.RARE, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));
        this.register("frost_protection", () -> new FrostProtectionEnchantment(Enchantment.Rarity.UNCOMMON, ARMOR_SLOTS));
    }
}

package auviotre.enigmatic.addon.contents.enchantments;

import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

public class FrostProtectionEnchantment extends Enchantment {
    public FrostProtectionEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.ARMOR, slots);
    }

    public int getMinCost(int level) {
        return 2 + level * 8;
    }

    public int getMaxCost(int level) {
        return this.getMinCost(level) + 8;
    }

    public int getMaxLevel() {
        return 4;
    }

    public int getDamageProtection(int level, DamageSource source) {
        return source.type().effects().equals(DamageEffects.FREEZING) ? level * 2 : 0;
    }

    public boolean checkCompatibility(Enchantment enchantment) {
        if (enchantment instanceof ProtectionEnchantment protection) {
            return protection.type == ProtectionEnchantment.Type.FALL;
        } else {
            return super.checkCompatibility(enchantment);
        }
    }
}

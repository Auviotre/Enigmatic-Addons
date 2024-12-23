package auviotre.enigmatic.addon.contents.enchantments;

import auviotre.enigmatic.addon.registries.EnigmaticAddonEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class FrostShatteringEnchantment extends Enchantment {
    public FrostShatteringEnchantment(Rarity rarity, EquipmentSlot[] slots) {
        super(rarity, EnchantmentCategory.WEAPON, slots);
    }

    public int getMinCost(int level) {
        return 16 + 20 * (level - 1);
    }

    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    public int getMaxLevel() {
        return 2;
    }

    public void doPostAttack(LivingEntity attacker, Entity target, int level) {
        if (target instanceof LivingEntity living && living.canFreeze()) {
            living.setTicksFrozen(living.getTicksFrozen() + 20 + 10 * level);
        }
    }

    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof AxeItem;
    }

    protected boolean checkCompatibility(Enchantment enchantment) {
        return enchantment != Enchantments.FIRE_ASPECT && enchantment != EnigmaticAddonEnchantments.FROST_ASPECT;
    }
}

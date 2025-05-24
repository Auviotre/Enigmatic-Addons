package auviotre.enigmatic.addon.contents.enchantments;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class FrostAspectEnchantment extends Enchantment {
    public FrostAspectEnchantment(Rarity rarity, EquipmentSlot[] slots) {
        super(rarity, EnchantmentCategory.WEAPON, slots);
    }

    public int getMinCost(int level) {
        return 10 + 20 * (level - 1);
    }

    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    public int getMaxLevel() {
        return 2;
    }

    public void doPostAttack(LivingEntity attacker, Entity target, int level) {
        if (target instanceof LivingEntity living && living.canFreeze()) {
            living.setTicksFrozen(living.getTicksFrozen() + 20 + 40 * level);
        }
    }

    protected boolean checkCompatibility(Enchantment enchantment) {
        return enchantment != Enchantments.FIRE_ASPECT && super.checkCompatibility(enchantment);
    }
}

package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EnigmaticPearl extends ItemBaseCurio {
    public EnigmaticPearl() {
        super(getDefaultProperties().fireResistant().rarity(EnigmaticLegacy.LEGENDARY));
    }

    public static boolean ava(ItemStack stack) {
        float ava = ItemNBTHelper.getFloat(stack, "BonusAva", 0.0F);
        return stack.getDisplayName().getString().equals("[#EnigamticEssence]");
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public CreativeModeTab getCreativeTab() {
        return null;
    }

    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean selected) {
        if (!level.isClientSide() && entity instanceof LivingEntity living && ava(itemStack)) {
            living.addEffect(new MobEffectInstance(EnigmaticAddonEffects.PURE_RESISTANCE_EFFECT, 50, 3, true, false));
        }
    }
}

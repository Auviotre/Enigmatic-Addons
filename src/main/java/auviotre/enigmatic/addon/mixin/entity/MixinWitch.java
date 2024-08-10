package auviotre.enigmatic.addon.mixin.entity;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;

@Mixin(Witch.class)
public abstract class MixinWitch extends Raider implements RangedAttackMob {
    @Unique
    private boolean enigmaticAddons$firstPotion = true;

    protected MixinWitch(EntityType<? extends Raider> type, Level level) {
        super(type, level);
    }

    @ModifyArg(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionUtils;setPotion(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/alchemy/Potion;)Lnet/minecraft/world/item/ItemStack;"), index = 1)
    public Potion performRangedAttackMix(Potion origin) {
        if (SuperAddonHandler.isCurseBoosted(this)) {
            if (origin.equals(Potions.HARMING)) {
                return Potions.STRONG_HARMING;
            } else if (origin.equals(Potions.HEALING)) {
                return Potions.STRONG_HEALING;
            } else if (origin.equals(Potions.REGENERATION)) {
                return Potions.STRONG_REGENERATION;
            } else if (origin.equals(Potions.SLOWNESS)) {
                return Potions.STRONG_SLOWNESS;
            } else if (origin.equals(Potions.POISON)) {
                return Potions.STRONG_POISON;
            } else if (origin.equals(Potions.WEAKNESS)) {
                return Potions.LONG_WEAKNESS;
            }
        }
        return origin;
    }

    @ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionUtils;getMobEffects(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    public ItemStack aiStepMix(ItemStack stack) {
        if (SuperAddonHandler.isCurseBoosted(this) && this.enigmaticAddons$firstPotion) {
            this.enigmaticAddons$firstPotion = false;
            ArrayList<MobEffectInstance> effects = new ArrayList<>();
            effects.add(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400));
            effects.add(new MobEffectInstance(MobEffects.WATER_BREATHING, 2400));
            effects.add(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400));
            effects.add(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 1));
            effects.add(new MobEffectInstance(MobEffects.HEAL, 10, 4));
            return PotionUtils.setCustomEffects(new ItemStack(Items.POTION), effects);
        }
        return stack;
    }
}

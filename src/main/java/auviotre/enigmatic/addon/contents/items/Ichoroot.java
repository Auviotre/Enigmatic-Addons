package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseFood;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Ichoroot extends ItemBaseFood {
    public Ichoroot() {
        super(getDefaultProperties().rarity(Rarity.RARE), new FoodProperties.Builder().nutrition(3).saturationMod(0.9F)
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 360), 0.25F).alwaysEat().build());
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.ichoroot");
    }

    public void onConsumed(Level world, @NotNull Player player, ItemStack food) {
        Collection<MobEffectInstance> activeEffects = player.getActiveEffects();
        if (!world.isClientSide()) {
            if (!activeEffects.isEmpty()) {
                ArrayList<MobEffect> effects = new ArrayList<>();
                for (MobEffectInstance activeEffect : activeEffects) {
                    if (!activeEffect.getEffect().isBeneficial()) effects.add(activeEffect.getEffect());
                }
                if (!effects.isEmpty()) player.removeEffect(effects.get(player.getRandom().nextInt(effects.size())));
            }
            int ichor = player.getPersistentData().getInt("Ichor");
            player.getPersistentData().putInt("Ichor", ichor + 200 + player.getRandom().nextInt(100));
        }
    }

    public int getUseDuration(ItemStack itemStack) {
        return 21;
    }
}

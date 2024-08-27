package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBasePotion;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class AstralPotion extends ItemBasePotion {
    public static Omniconfig.IntParameter cooldown;
    public static Omniconfig.DoubleParameter durationMultiplier;

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("PotionofCosmic");
        cooldown = builder.comment("The cooldown of Potion of Cosmic. Measured in ticks.").max(32000).getInt("Cooldown", 2400);
        durationMultiplier = builder.comment("The Duration Multiplier of Potion of Cosmic.").max(32.0).getDouble("DurationMultiplier", 1.6);
        builder.popPrefix();
    }

    public AstralPotion() {
        super(getDefaultProperties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.astralPotion1");
            if (ItemNBTHelper.getBoolean(stack, "isDrunk", false))
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.astralPotion2");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof Player player && !entityIn.level().isClientSide) {
            if (player.getPersistentData().getInt("CosmicPotion") > 0) {
                if (!ItemNBTHelper.getBoolean(stack, "isDrunk", false)) {
                    ItemNBTHelper.setBoolean(stack, "isDrunk", true);
                }
            } else if (ItemNBTHelper.getBoolean(stack, "isDrunk", false)) {
                ItemNBTHelper.setBoolean(stack, "isDrunk", false);
            }
        }
    }

    public void onConsumed(Level world, Player player, ItemStack potion) {
        world.playSound(null, player.blockPosition(), EnigmaticSounds.CHARGED_ON, SoundSource.PLAYERS, (float) (0.8 + Math.random() * 0.2), (float) (0.8 + Math.random() * 0.2));

        if (player.getPersistentData().getInt("CosmicPotion") <= 0) {
            Collection<MobEffectInstance> activeEffects = player.getActiveEffects();
            if (!activeEffects.isEmpty()) {
                player.getPersistentData().putInt("CosmicPotion", cooldown.getValue());
                for (MobEffectInstance effect : activeEffects) {
                    if (effect.duration > 0) {
                        effect.duration = (int) (durationMultiplier.getValue() * effect.duration);
                    }
                }
            }
        } else {
            player.getPersistentData().remove("CosmicPotion");
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 600));
        }
    }
}

package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class VoidTome extends ItemBase {
    public VoidTome() {
        super(ItemBase.getDefaultProperties().rarity(Rarity.RARE).stacksTo(1));
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.voidTome");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (world.isClientSide) {
            float width = player.getBbWidth() * 1.25F;
            for (int i = 0; i < 24; i++) {
                world.addParticle(ParticleTypes.SQUID_INK, player.getRandomX(width), player.getRandomY(), player.getRandomZ(width), 0.0, 0.05, 0.0F);
            }
        } else {
            SuperpositionHandler.setPersistentBoolean(player, "UsedVoidTome", true);
            if (SuperAddonHandler.unlockSpecialSlot("scroll", player)) {
                player.level().playLocalSound(player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F, true);
            }
        }
        player.hurt(player.damageSources().fellOutOfWorld(), player.getHealth() * 0.3F);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400, 0, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 400, 0, false, true));
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) itemStack.shrink(1);
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }
}

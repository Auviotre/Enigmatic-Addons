package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.TheAcknowledgment;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class TheBless extends TheAcknowledgment implements ICursed {
    public static Omniconfig.DoubleParameter attackDamage;
    public static Omniconfig.DoubleParameter attackSpeed;
    public static Omniconfig.IntParameter invulnerableTime;

    public TheBless() {
        super(getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).fireResistant(), "the_bless", attackDamage.getValue(), attackSpeed.getValue());
        this.setAllowAllEnchantments(true);
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("TheBless");
        attackDamage = builder.comment("Attack damage of The Bless, actual damage shown in tooltip will be ( 1 + this value ).").max(32768.0).getDouble("AttackDamage", 6.0);
        attackSpeed = builder.comment("Attack speed of The Bless.").minMax(32768.0).getDouble("AttackSpeed", -1.6);
        invulnerableTime = builder.comment("The invulnerable time after hit while The Bless in inventory.").min(40).max(200).getInt("InvulnerableTime", 40);
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> list, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            if (Minecraft.getInstance().player != null && SuperAddonHandler.isOKOne(Minecraft.getInstance().player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.theTwist4");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.theTwist5");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            }
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.theBless3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.theBless4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.theBless5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.theBless6");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.theBless1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.theBless2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
        if (stack.isEnchanted()) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setSecondsOnFire(10);
        if (attacker.getRandom().nextInt(5) == 0) {
            target.addEffect(new MobEffectInstance(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT, 100), attacker);
            target.playSound(SoundEvents.TRIDENT_HIT, 0.5F, 0.1F);
        }
        List<MobEffectInstance> effects = target.getActiveEffects().stream().toList();
        for (MobEffectInstance effect : effects) {
            if (effect.getEffect().isBeneficial()) target.removeEffect(effect.getEffect());
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!SuperAddonHandler.isOKOne(player)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        } else {
            if (hand == InteractionHand.MAIN_HAND) {
                ItemStack offhandStack = player.getOffhandItem();
                if (offhandStack != null && offhandStack.getItem().getUseAnimation(offhandStack) == UseAnim.BLOCK) {
                    return InteractionResultHolder.pass(player.getItemInHand(hand));
                }
            }
            return super.use(world, player, hand);
        }
    }
}

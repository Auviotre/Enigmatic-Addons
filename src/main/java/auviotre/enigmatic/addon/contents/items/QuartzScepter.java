package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.contents.entities.ThrownQuartzDagger;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;

public class QuartzScepter extends ItemBase {
    public static Omniconfig.IntParameter shootInterval;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public QuartzScepter() {
        super(ItemBase.getDefaultProperties().rarity(Rarity.RARE).stacksTo(1).durability(160));
        MinecraftForge.EVENT_BUS.register(this);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 1.5, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -1.6F, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("QuartzScepter");
        shootInterval = builder.comment("The shoot interval time of daggers. Measured as ticks.").max(40).min(1).getInt("ShootInterval", 8);
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.quartzScepter1");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.quartzScepter2");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.quartzScepter3");
        if (stack.isEnchanted()) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity user) {
        stack.hurtAndBreak(2, user, consumer -> consumer.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemInHand);
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (user instanceof Player player) {
            int cooldown = 40;
            if (!SuperpositionHandler.hasItem(player, EnigmaticAddonItems.ARTIFICIAL_FLOWER)) cooldown += 20;
            stack.hurtAndBreak(this.getUseDuration(stack) / 8, player, consumer -> consumer.broadcastBreakEvent(player.getUsedItemHand()));
            player.getCooldowns().addCooldown(this, cooldown);
            player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS);
            player.swing(player.getUsedItemHand());
        }
        return stack;
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int tick) {
        if (entity instanceof Player player) {
            int pass = this.getUseDuration(stack) - tick;
            stack.hurtAndBreak(pass / 8, player, consumer -> consumer.broadcastBreakEvent(player.getUsedItemHand()));
            if (!SuperpositionHandler.hasItem(player, EnigmaticAddonItems.ARTIFICIAL_FLOWER)) pass += 50;
            player.getCooldowns().addCooldown(this, 20 + pass / 5);
            player.swing(player.getUsedItemHand());
        }
    }

    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int tick) {
        int delay = shootInterval.getValue();
        if (SuperpositionHandler.hasCurio(user, EnigmaticAddonItems.QUARTZ_RING)) delay -= 2;
        if (tick % delay == 0 && !level.isClientSide && user instanceof Player player) {
            ThrownQuartzDagger dagger = new ThrownQuartzDagger(player, level);
            RandomSource random = player.getRandom();
            double x = player.getX() + (random.nextFloat() - 0.5F) * 0.6F;
            double y = player.getEyeY() - 0.40000000149011612 + random.nextFloat() * 0.6F;
            double z = player.getZ() + (random.nextFloat() - 0.5F) * 0.6F;
            dagger.setPos(x, y, z);
            dagger.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, SuperpositionHandler.hasCurio(user, EnigmaticAddonItems.QUARTZ_RING) ? 1.6F : 1.2F, 1F);
            double value = player.getAttribute(Attributes.LUCK).getValue();
            int luck = (int) Math.max(Math.floor(value * 0.8F + 0.5F), 0);
            dagger.setBaseDamage(dagger.getBaseDamage() + luck);
            dagger.setNoGravity(true);
            level.addFreshEntity(dagger);
            level.playSound(null, player, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.2F, 1.6F);
        }
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getAttributeModifiers(slot, stack);
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 32;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public int getUseDuration(ItemStack stack) {
        return 100;
    }

    public boolean isValidRepairItem(ItemStack self, ItemStack stack) {
        return stack.is(Items.QUARTZ) || super.isValidRepairItem(self, stack);
    }
}
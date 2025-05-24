package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.contents.entities.UltimatePotionEntity;
import auviotre.enigmatic.addon.helpers.PotionAddonHelper;
import auviotre.enigmatic.addon.registries.EnigmaticAddonPotions;
import com.aizistral.enigmaticlegacy.api.items.IAdvancedPotionItem;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.objects.AdvancedPotion;
import com.aizistral.enigmaticlegacy.registries.EnigmaticPotions;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public abstract class UltimatePotionAddon extends ItemBase implements IAdvancedPotionItem {
    public IAdvancedPotionItem.PotionType potionType;

    public UltimatePotionAddon(Rarity rarity, IAdvancedPotionItem.PotionType type) {
        super(ItemBase.getDefaultProperties().rarity(rarity).stacksTo(1));
        this.potionType = type;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack stack) {
        return this.potionType == PotionType.ULTIMATE;
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance().copy();
        PotionHelper.setAdvancedPotion(stack, EnigmaticPotions.EMPTY_POTION);
        return stack.copy();
    }

    public String getDescriptionId(ItemStack stack) {
        String id = this.getDescriptionId();
        return id + ".effect." + PotionAddonHelper.getAdvancedPotion(stack).getId();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        SuperpositionHandler.addPotionTooltip(PotionAddonHelper.getEffects(stack), stack, list, 1.0F);
    }

    public CreativeModeTab getCreativeTab() {
        return EnigmaticTabs.POTIONS;
    }

    public List<ItemStack> getCreativeTabStacks() {
        ImmutableList.Builder<ItemStack> items = ImmutableList.builder();
        Iterator<AdvancedPotion> iterator;
        AdvancedPotion potion;
        ItemStack stack;
        if (this.potionType == PotionType.COMMON) {
            iterator = EnigmaticAddonPotions.COMMON_POTIONS.iterator();
        } else {
            iterator = EnigmaticAddonPotions.ULTIMATE_POTIONS.iterator();
        }
        while (iterator.hasNext()) {
            potion = iterator.next();
            stack = new ItemStack(this);
            ItemNBTHelper.setString(stack, "EnigmaticPotion", potion.getId());
            items.add(stack);
        }
        return items.build();
    }

    public IAdvancedPotionItem.PotionType getPotionType() {
        return this.potionType;
    }

    public static class Base extends UltimatePotionAddon {
        public Base(Rarity rarity, PotionType type) {
            super(rarity, type);
        }

        public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
            Player player = entityLiving instanceof Player ? (Player) entityLiving : null;
            List<MobEffectInstance> effectList = PotionAddonHelper.getEffects(stack);
            if (player == null || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, stack);
            }

            if (!worldIn.isClientSide) {
                for (MobEffectInstance effect : effectList) {
                    if (effect.getEffect().isInstantenous()) {
                        effect.getEffect().applyInstantenousEffect(player, player, entityLiving, effect.getAmplifier(), 1.0);
                    } else {
                        entityLiving.addEffect(new MobEffectInstance(effect));
                    }
                }
            }

            if (player != null) {
                player.awardStat(Stats.ITEM_USED.get(this));
            }
            if (player == null || !player.getAbilities().instabuild) {
                if (stack.isEmpty()) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                }
                if (player != null) {
                    player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                }
            }
            return stack;
        }

        public int getUseDuration(ItemStack stack) {
            return 32;
        }

        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.DRINK;
        }

        public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
            playerIn.startUsingItem(handIn);
            return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
        }
    }

    public static class Splash extends UltimatePotionAddon {
        public Splash(Rarity rarity, PotionType type) {
            super(rarity, type);
        }

        public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
            ItemStack itemstack = playerIn.getItemInHand(handIn);
            ItemStack thrown = playerIn.getAbilities().instabuild ? itemstack.copy() : itemstack.split(1);
            worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.LINGERING_POTION_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            if (!worldIn.isClientSide) {
                UltimatePotionEntity potionEntity = new UltimatePotionEntity(worldIn, playerIn);
                potionEntity.setItem(thrown);
                potionEntity.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), -20.0F, 0.5F, 1.0F);
                potionEntity.setOwner(playerIn);
                worldIn.addFreshEntity(potionEntity);
            }

            playerIn.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(itemstack);
        }
    }

    public static class Lingering extends UltimatePotionAddon {
        public Lingering(Rarity rarity, PotionType type) {
            super(rarity, type);
        }

        public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
            ItemStack itemstack = playerIn.getItemInHand(handIn);
            ItemStack thrown = playerIn.getAbilities().instabuild ? itemstack.copy() : itemstack.split(1);
            worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.LINGERING_POTION_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            if (!worldIn.isClientSide) {
                UltimatePotionEntity potionEntity = new UltimatePotionEntity(worldIn, playerIn);
                potionEntity.setItem(thrown);
                potionEntity.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), -20.0F, 0.5F, 1.0F);
                potionEntity.setOwner(playerIn);
                worldIn.addFreshEntity(potionEntity);
            }

            playerIn.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(itemstack);
        }
    }
}

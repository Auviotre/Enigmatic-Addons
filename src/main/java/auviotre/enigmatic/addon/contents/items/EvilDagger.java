package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.entities.ThrownEvilDagger;
import auviotre.enigmatic.addon.packets.clients.PacketEvilCage;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EvilDagger extends ItemBase implements Vanishable, ICursed {
    private static final AttributeModifier EVIL_CURSE_MODIFIER = new AttributeModifier(UUID.fromString("637f51eb-218e-46aa-bcd3-10380acfd2d6"), "Evil Curse Modifier", -0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
    public static Omniconfig.IntParameter cooldown;
    public static Omniconfig.IntParameter curseHurtInterval;
    public static Omniconfig.DoubleParameter curseHurtAmount;
    public static Omniconfig.PerhapsParameter curseDamageRatio;

    public EvilDagger() {
        super(ItemBase.getDefaultProperties().fireResistant().rarity(Rarity.EPIC).durability(640));
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("TheCurseCarver");
        cooldown = builder.comment("The cooldown of usage. Measured as tick.").max(32768).min(20).getInt("Cooldown", 40);
        curseHurtInterval = builder.comment("The hurt interval time of cursed target. Measured as ticks.").max(40).min(1).getInt("CurseHurtInterval", 12);
        curseHurtAmount = builder.comment("The hurt damage amount of cursed target.").max(32768).min(0).getDouble("CurseHurtAmount", 2.5);
        curseDamageRatio = builder.comment("The initial health percentage the curse will totally damaged.").max(100).min(10).getPerhaps("CurseDamageRatio", 20);
        builder.popPrefix();
    }

    public static void EvilCursing(LivingEntity entity) {
        if (entity.getPersistentData().getBoolean("EvilCrashed")) {
            if (!entity.getAttribute(Attributes.ATTACK_DAMAGE).hasModifier(EVIL_CURSE_MODIFIER))
                entity.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(EVIL_CURSE_MODIFIER);
            entity.getPersistentData().remove("EvilCurseThreshold");
            return;
        }
        int percent = (int) (entity.getHealth() * 100 / entity.getMaxHealth());
        int threshold = Math.max(entity.getPersistentData().getInt("EvilCurseThreshold"), 1);
        Vec3 location = entity.position();
        float width = entity.getBbWidth() / 2;
        if (percent > threshold) {
            if (entity.tickCount % EvilDagger.curseHurtInterval.getValue() == 0) {
                EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(location.x, location.y, location.z, 64.0, entity.level().dimension())),
                        new PacketEvilCage(location.x, location.y, location.z, width, entity.getBbHeight(), 0));
                entity.hurt(entity.damageSources().source(EnigmaticAddonDamageTypes.EVIL_CURSE, null), (float) EvilDagger.curseHurtAmount.getValue());
                entity.setDeltaMovement(Vec3.ZERO);
                entity.hasImpulse = true;
                entity.invulnerableTime = 1;
            }
        } else if (percent <= threshold) {
            entity.getPersistentData().putBoolean("EvilCrashed", true);
            entity.setDeltaMovement(new Vec3(0.0, -1.0, 0.0));
            float damage = entity.getMaxHealth() * EvilDagger.curseDamageRatio.getValue().asModifier() / 5.0F;
            ((ServerLevel) entity.level()).sendParticles(ParticleTypes.EXPLOSION, location.x, location.y, location.z, 5, width, entity.getBbHeight(), width, 0.0);
            entity.hurt(entity.damageSources().source(EnigmaticAddonDamageTypes.EVIL_CURSE, null), (float) Math.max(damage, EvilDagger.curseHurtAmount.getValue() * 2));
        }
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int id, boolean selected) {
        super.inventoryTick(stack, level, entity, id, selected);
        if (entity.getPersistentData().getBoolean("EvilCrashed")) {
            entity.getPersistentData().remove("EvilCrashed");
            if (entity instanceof LivingEntity living)
                living.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(EVIL_CURSE_MODIFIER);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.evilDagger1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.evilDagger2");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
        if (stack.isEnchanted()) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (SuperpositionHandler.isTheCursedOne(player) && stack.getMaxDamage() > stack.getDamageValue() + 80) {
            player.getCooldowns().addCooldown(this, player.getAbilities().instabuild ? 15 : EvilDagger.cooldown.getValue());
            stack.hurtAndBreak(10, player, (consumer) -> consumer.broadcastBreakEvent(player.getUsedItemHand()));
            int curseAmount = SuperpositionHandler.getCurseAmount(stack);
            if (!level.isClientSide) {
                ThrownEvilDagger dagger = new ThrownEvilDagger(player, level);
                double damage = dagger.getBaseDamage() + stack.getEnchantmentLevel(Enchantments.SHARPNESS) * 0.2 + curseAmount * 0.25;
                dagger.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.25F, 0.0F);
                dagger.setNoGravity(true);
                dagger.setCurseAmount(curseAmount);
                dagger.setBaseDamage(damage);
                level.addFreshEntity(dagger);
                if (stack.getEnchantmentLevel(Enchantments.MULTISHOT) > 0) {
                    for (int i = 0; i < 2; i++) {
                        dagger = new ThrownEvilDagger(player, level);
                        dagger.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3F, 9.6F);
                        dagger.setNoGravity(true);
                        dagger.setCurseAmount(curseAmount);
                        dagger.setBaseDamage(damage);
                        level.addFreshEntity(dagger);
                    }
                }
            }
            player.swing(hand);
            level.playSound(null, player, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 2.0F);
            return InteractionResultHolder.consume(stack);
        } else return InteractionResultHolder.pass(stack);
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity user) {
        stack.hurtAndBreak(1, user, (consumer) -> consumer.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        if (user instanceof Player player && SuperpositionHandler.isTheCursedOne(player)) {
            int curseAmount = SuperpositionHandler.getCurseAmount(stack);
            int threshold = entity.getPersistentData().getInt("EvilCurseThreshold");
            if (threshold > 0) {
                entity.getPersistentData().putInt("EvilCurseThreshold", Math.max(threshold - 3 - curseAmount, 1));
            }
        }
        return true;
    }

    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, (user) -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    public boolean isValidRepairItem(ItemStack self, ItemStack stack) {
        return stack.is(EnigmaticItems.EVIL_INGOT) || super.isValidRepairItem(self, stack);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        int curseAmount = SuperpositionHandler.getCurseAmount(stack);
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 5.0 + curseAmount * 0.5, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -1.6F, AttributeModifier.Operation.ADDITION));
        return slot == EquipmentSlot.MAINHAND ? builder.build() : super.getAttributeModifiers(slot, stack);
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.isCurse() || (enchantment.category.equals(EnchantmentCategory.WEAPON) || enchantment.category.equals(EnchantmentCategory.BREAKABLE) || enchantment == Enchantments.MULTISHOT) && enchantment != Enchantments.SWEEPING_EDGE;
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 28;
    }
}

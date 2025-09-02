package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.packets.clients.PacketForgottenIce;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ISpellstone;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ForgottenIce extends ItemSpellstoneCurio implements ISpellstone {
    public static final AttributeModifier HARD_FROZEN = new AttributeModifier(UUID.fromString("7d1f95a0-39d9-4cae-992a-74e6636b3b5b"), "Forgotten Ice Frozen", -2.0F, AttributeModifier.Operation.MULTIPLY_TOTAL);
    public static Omniconfig.IntParameter spellstoneCooldown;
    public static Omniconfig.PerhapsParameter frostBoost;
    public static Omniconfig.PerhapsParameter multiResistance;
    public static Omniconfig.DoubleParameter spellstoneDamageBase;
    public static Omniconfig.DoubleParameter spellstoneDamagePerFrozenSec;

    public ForgottenIce() {
        super(ItemSpellstoneCurio.getDefaultProperties().rarity(Rarity.RARE));
        this.immunityList.add(DamageTypes.FREEZE);
        float modifier = multiResistance.getValue().asModifierInverted();
        this.resistanceList.put(DamageTypes.MOB_PROJECTILE, () -> modifier);
        this.resistanceList.put(DamageTypes.SONIC_BOOM, () -> modifier);
        this.resistanceList.put(DamageTypes.IN_FIRE, () -> 2.5F);
        this.resistanceList.put(DamageTypes.ON_FIRE, () -> 2.5F);
        this.resistanceList.put(DamageTypes.LAVA, () -> 2.5F);
        this.resistanceList.put(DamageTypes.HOT_FLOOR, () -> 2.5F);
        this.resistanceList.put(DamageTypes.FIREBALL, () -> 2.5F);
        this.resistanceList.put(DamageTypes.FALL, () -> 1.6F);
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("ForgottenIceCrystal");
        spellstoneCooldown = builder.comment("Active ability cooldown for Forgotten Ice Crystal. Measured in ticks. 20 ticks equal to 1 second.").getInt("Cooldown", 240);
        spellstoneDamageBase = builder.comment("The extra damage of ability of Forgotten Ice Crystal.").getDouble("ExtraDamageBase", 2.0);
        spellstoneDamagePerFrozenSec = builder.comment("The extra damage ratio per every frozen second of ability of Forgotten Ice Crystal.").getDouble("ExtraDamagePerSec", 0.01);
        frostBoost = builder.comment("The damage boost on target which has been fully frozen. Defined as percentage.").max(256.0).getPerhaps("FrostBoost", 30);
        multiResistance = builder.comment("Resistance to projectile and sonic attacks provided by Forgotten Ice Crysta. Defined as percentage.").max(100.0).getPerhaps("Resistance", 30);
        builder.popPrefix();
    }

    public static ImmutableMultimap<Attribute, AttributeModifier> getFrozenAttributes() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.MOVEMENT_SPEED, HARD_FROZEN);
        return builder.build();
    }

    public int getCooldown(Player player) {
        return player != null && (reducedCooldowns.test(player) || player.getAbilities().instabuild) ? 15 : spellstoneCooldown.getValue();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgottenIce1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgottenIce2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgottenIceCooldown", ChatFormatting.GOLD, (float) this.getCooldown(Minecraft.getInstance().player) / 20.0F);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgottenIce3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgottenIce4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgottenIce5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgottenIce6");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgottenIce7");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgottenIce8", ChatFormatting.GOLD, multiResistance + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.forgottenIce9");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
        try {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.currentKeybind", ChatFormatting.LIGHT_PURPLE, KeyMapping.createNameSupplier("key.spellstoneAbility").get().getString().toUpperCase());
        } catch (NullPointerException ignored) {
        }
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0));
            for (LivingEntity entity : entities) {
                if (entity.isFullyFrozen()) {
                    int tick = entity.getPersistentData().getInt("ForgottenFrozenTick");
                    if (entity.getTicksFrozen() > 340) {
                        tick++;
                        entity.getPersistentData().putBoolean("ForgottenFrozenHard", true);
                    }
                    entity.getPersistentData().putInt("ForgottenFrozenTick", tick + 1);
                }
            }
        }
    }

    public void triggerActiveAbility(Level world, ServerPlayer player, ItemStack stack) {
        if (!SuperpositionHandler.hasSpellstoneCooldown(player)) {
            ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Blocks.BLUE_ICE));
            SimpleParticleType snowflake = ParticleTypes.SNOWFLAKE;
            List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0));
            float damage = (float) (spellstoneDamageBase.getValue() + player.getAttributeValue(Attributes.ATTACK_DAMAGE));
            for (LivingEntity entity : entities) {
                if (entity.is(player)) continue;
                int tick = entity.getPersistentData().getInt("ForgottenFrozenTick");
                float extra = 0;
                if (entity.isFullyFrozen())
                    extra = (float) (spellstoneDamagePerFrozenSec.getValue() * Math.min(tick, 400) / 2);
                if (entity.canFreeze()) entity.setTicksFrozen(entity.getTicksFrozen() + 500);
                entity.hurt(player.damageSources().source(DamageTypes.FREEZE, player), damage * (1 + extra));
                if (tick <= 400) entity.getPersistentData().remove("ForgottenFrozenTick");
                else entity.getPersistentData().putInt("ForgottenFrozenTick", (tick - 400) / 2);
                float width = entity.getBbWidth() / 1.8F;
                ((ServerLevel) entity.level()).sendParticles(particle, entity.getX(), entity.getY(0.8F), entity.getZ(), 16, width, 0.1D, width, 0.0D);
                ((ServerLevel) entity.level()).sendParticles(snowflake, entity.getX(), entity.getY(0.8F), entity.getZ(), 10, width, 0.1D, width, 0.0D);
            }
            EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(player.getX(), player.getY(), player.getZ(), 16.0, player.level().dimension())),
                    new PacketForgottenIce(player.getX(), player.getY(), player.getZ()));
            SuperpositionHandler.setSpellstoneCooldown(player, this.getCooldown(player));
        }
    }
}

package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.contents.entities.SoulFlameBall;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ISpellstone;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class IllusionLantern extends ItemSpellstoneCurio implements ISpellstone {
    private static final List<TagKey<Block>> PLANT_SET = List.of(
            BlockTags.FLOWERS,
            BlockTags.SAPLINGS,
            BlockTags.REPLACEABLE,
            BlockTags.REPLACEABLE_BY_TREES,
            BlockTags.SWORD_EFFICIENT
    );
    private static final List<TagKey<DamageType>> BYPASS_SET = List.of(
            DamageTypeTags.BYPASSES_INVULNERABILITY,
            DamageTypeTags.BYPASSES_RESISTANCE,
            DamageTypeTags.BYPASSES_ARMOR,
            DamageTypeTags.BYPASSES_ENCHANTMENTS,
            DamageTypeTags.BYPASSES_EFFECTS
    );
    public static Omniconfig.IntParameter spellstoneCooldown;
    public static Omniconfig.IntParameter fireballCooldown;
    public static Omniconfig.PerhapsParameter nonMagicDamageMultiplier;
    public static Omniconfig.PerhapsParameter bypassDamageResistance;

    public IllusionLantern() {
        super(ItemSpellstoneCurio.getDefaultProperties().rarity(Rarity.RARE));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("SoulLanternofIllusion");
        spellstoneCooldown = builder.comment("Active ability cooldown for Soul Lantern of Illusion. Measured in ticks. 20 ticks equal to 1 second.").getInt("Cooldown", 0);
        fireballCooldown = builder.comment("The cooldown of soul fireball generating for Soul Lantern of Illusion. Measured in ticks.").getInt("FireballCooldown", 60);
        nonMagicDamageMultiplier = builder.comment("The damage multiplier of the non-magic damage you received. Measured in percentage.").min(0).getPerhaps("NonMagicDamageMultiplier", 25);
        bypassDamageResistance = builder.comment("The damage resistance multiplier of the bypass-armor damage you received. Measured in percentage.").max(80).getPerhaps("BypassDamageResistance", 50);
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.illusionLantern1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.illusionLantern2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.illusionLanternCooldown", ChatFormatting.GOLD, spellstoneCooldown.getValue() / 20.0F);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.illusionLantern3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.illusionLantern4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.illusionLantern5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.illusionLantern6");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.illusionLantern7");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.illusionLantern8");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.illusionLantern9");
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
        if (context.entity() instanceof ServerPlayer player) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                List<SoulFlameBall> balls = player.level().getEntitiesOfClass(SoulFlameBall.class, player.getBoundingBox().inflate(3.0));
                if (balls.size() < 5) {
                    player.getCooldowns().addCooldown(this, fireballCooldown.getValue());
                    SoulFlameBall soulFlameBall = new SoulFlameBall(player.level(), player);
                    soulFlameBall.getPersistentData().putInt("BallID", balls.size());
                    player.level().addFreshEntity(soulFlameBall);
                }
            }
            if (player.tickCount % 20 == 0 && !(player.isCreative() || player.isSpectator())) {
                Level level = player.level();
                BlockPos blockPos = player.blockPosition();
                boolean seeSky = level.canSeeSkyFromBelowWater(blockPos);
                int rawBrightness = level.getLightEngine().getRawBrightness(blockPos, 8);
                if (seeSky && rawBrightness > 9) {
                    player.hurt(SuperAddonHandler.simpleSource(player, EnigmaticAddonDamageTypes.EVIL_CURSE), player.getMaxHealth() / 5.0F);
                }

                Iterable<BlockPos> posSet = BlockPos.betweenClosed(blockPos.offset(-5, -5, -5), blockPos.offset(5, 5, 5));
                for (BlockPos pos : posSet) {
                    BlockState blockState = level.getBlockState(pos);
                    if (PLANT_SET.stream().anyMatch(blockState::is)) {
                        level.destroyBlock(pos, false, player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (entity instanceof Player player && SuperpositionHandler.hasCurio(player, this)) {
            boolean magic = source.type().msgId().contains("magic");
            boolean spell = source.type().msgId().contains("spell");
            boolean witch = source.is(DamageTypeTags.WITCH_RESISTANT_TO);
            if (!(magic || spell || witch))
                event.setAmount(event.getAmount() * nonMagicDamageMultiplier.getValue().asModifier(true));
            if (BYPASS_SET.stream().anyMatch(source::is)) {
                event.setAmount(event.getAmount() * (1 - bypassDamageResistance.getValue().asModifier()));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (BYPASS_SET.stream().anyMatch(event.getSource()::is)) return;
        if (entity instanceof Player player && SuperpositionHandler.hasCurio(player, this)) {
            Predicate<LivingEntity> noSharing = living -> {
                if (living instanceof OwnableEntity own && own.getOwner() == player) return false;
                else if (living.equals(event.getSource().getEntity())) return false;
                else if (living.equals(player) || player.isAlliedTo(living)) return false;
                return living.isAlive() && !SuperpositionHandler.hasCurio(living, this);
            };
            List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8), noSharing);
            if (entities.isEmpty()) return;
            int size = entities.size();
            float baseDamage = event.getAmount() / (size + 1) / (size + 1);
            event.setAmount(baseDamage * (2 * size + 1));
            for (LivingEntity livingEntity : entities) {
                livingEntity.hurt(SuperAddonHandler.damageSource(livingEntity, DamageTypes.MAGIC, player), baseDamage * size);
            }
        }
    }
}

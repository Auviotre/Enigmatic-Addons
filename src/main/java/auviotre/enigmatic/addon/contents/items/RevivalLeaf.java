package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ISpellstone;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ExperienceHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.*;

public class RevivalLeaf extends ItemSpellstoneCurio implements ISpellstone {
    private static final List<TagKey<Block>> PLANT_SET = List.of(
            BlockTags.FLOWERS,
            BlockTags.SAPLINGS,
            BlockTags.REPLACEABLE_BY_TREES
    );
    public static Omniconfig.IntParameter spellstoneCooldown;
    public static Omniconfig.IntParameter naturalRegenerationSpeed;
    public static Omniconfig.DoubleParameter skillRadius;
    public static Omniconfig.IntParameter poisonTime;
    public static Omniconfig.IntParameter poisonLevel;
    public static Omniconfig.IntParameter regenerationTime;
    public static Omniconfig.IntParameter regenerationLevel;
    public Map<Player, Integer> flyMap = new WeakHashMap<>();

    public RevivalLeaf() {
        super(ItemSpellstoneCurio.getDefaultProperties().rarity(Rarity.RARE));
        this.immunityList.add(DamageTypes.WITHER);
        this.resistanceList.put(DamageTypes.IN_FIRE, () -> 2.0F);
        this.resistanceList.put(DamageTypes.ON_FIRE, () -> 2.0F);
        this.resistanceList.put(DamageTypes.LAVA, () -> 2.0F);
        this.resistanceList.put(DamageTypes.HOT_FLOOR, () -> 2.0F);
        this.resistanceList.put(DamageTypes.FIREBALL, () -> 2.0F);
        this.resistanceList.put(DamageTypes.MOB_PROJECTILE, () -> 1.5F);
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("RevivalLeaf");
        spellstoneCooldown = builder.comment("Active ability cooldown for Revival Leaf. Measured in ticks. 20 ticks equal to 1 second.").getInt("Cooldown", 320);
        naturalRegenerationSpeed = builder.comment("The time required for each 0.5HP treatment from the natural regeneration of the Revival Leaf. Measured in ticks.").min(5).getInt("NaturalRegenerationTick", 40);
        skillRadius = builder.comment("The effect radius of Revival Leaf' ability.").getDouble("AbilityRadius", 5.0);
        poisonTime = builder.comment("Amount of ticks for which bearer of the leaf will apply Poison effect to entities they attack. 20 ticks equals to 1 second.").getInt("PoisonTime", 160);
        poisonLevel = builder.comment("Level of Poison that bearer of the leaf will apply to entities they attack.").max(3.0).getInt("PoisonLevel", 1);
        regenerationTime = builder.comment("Amount of ticks for which bearer of the leaf will apply Regeneration effect to entities nearby when ability activated.").getInt("RegenerationTime", 180);
        regenerationLevel = builder.comment("Level of Regeneration that bearer of the leaf will apply to entities nearby when ability activated.").max(3.0).getInt("RegenerationLevel", 1);
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeaf1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeaf2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeafCooldown", ChatFormatting.GOLD, (float) this.getCooldown(Minecraft.getInstance().player) / 20.0F);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeaf3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeaf4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeaf5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeaf6");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeaf7");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeaf8");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeaf9");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.revivalLeaf10");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
        try {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.currentKeybind", ChatFormatting.LIGHT_PURPLE, KeyMapping.createNameSupplier("key.spellstoneAbility").get().getString().toUpperCase());
        } catch (NullPointerException ignored) {
        }
    }

    public int getCooldown(Player player) {
        return player != null && reducedCooldowns.test(player) ? 200 : spellstoneCooldown.getValue();
    }

    public void triggerActiveAbility(Level world, ServerPlayer player, ItemStack stack) {
        if (!SuperpositionHandler.hasSpellstoneCooldown(player)) {
            int level = ExperienceHelper.getPlayerXPLevel(player);
            int playerXP = ExperienceHelper.getPlayerXP(player);
            if (playerXP > 10) {
                ExperienceHelper.drainPlayerXP(player, Math.min(Mth.ceil(5 * player.getRandom().nextFloat()) + level, playerXP));
                BlockPos pos = player.blockPosition();
                world.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, (float) (0.8 + Math.random() * 0.2));
                SuperpositionHandler.setSpellstoneCooldown(player, this.getCooldown(player));
                List<LivingEntity> genericMobs = player.level().getEntitiesOfClass(LivingEntity.class, SuperpositionHandler.getBoundingBoxAroundEntity(player, skillRadius.getValue()));
                for (LivingEntity mob : genericMobs) {
                    if (level > 25) {
                        float maxHealth = mob.getMaxHealth();
                        mob.heal(Math.min(0.2F, (level - 25) * 0.01F) * maxHealth);
                    }
                    mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenerationTime.getValue() + Math.min(playerXP * level / 2, regenerationTime.getValue()), regenerationLevel.getValue(), false, true));
                }
                Iterable<BlockPos> iterable = BlockPos.betweenClosed(pos.offset(5, 5, 5), pos.offset(-5, -5, -5));
                for (BlockPos blockPos : iterable) {
                    if (world.getBlockState(blockPos).is(Blocks.WITHER_ROSE)) {
                        world.destroyBlock(blockPos, false);
                        world.setBlock(blockPos, Blocks.POPPY.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        if (entity instanceof Player player) {
            if (!player.getActiveEffects().isEmpty()) {
                Iterator<MobEffectInstance> effects = (new ArrayList<>(player.getActiveEffects())).iterator();
                MobEffectInstance activeEffect;
                while (effects.hasNext()) {
                    activeEffect = effects.next();
                    if (activeEffect.getEffect() == MobEffects.HUNGER || activeEffect.getEffect() == MobEffects.WITHER || activeEffect.getEffect() == MobEffects.POISON) {
                        player.removeEffect(activeEffect.getEffect());
                    }
                }
            }

            if (!SuperpositionHandler.hasCurio(player, EnigmaticItems.FABULOUS_SCROLL) && !SuperpositionHandler.hasCurio(player, EnigmaticItems.HEAVEN_SCROLL)) {
                boolean flag = !player.isSpectator() && !player.isCreative() && player.getAbilities().flying;
                if (hasPlantBy(player) && flag) {
                    player.addDeltaMovement(new Vec3(0, -0.012F, 0));
                }
                if (player.level().isClientSide()) {
                    if (player.tickCount % 12 == 0 && flag) {
                        BlockPos lazyPos = BlockPos.of(player.getPersistentData().getLong("RevivalFlightLazyPos"));
                        Vec3 dist = player.position().subtract(lazyPos.getCenter());
                        player.level().addParticle(ParticleTypes.HAPPY_VILLAGER, player.getRandomX(0.5), player.getY(), player.getRandomZ(0.5), 0, 0, 0);
                        int[] offset = {0, 1};
                        for (int x : offset)
                            for (int y : offset)
                                for (int z : offset) {
                                    player.level().addParticle(ParticleTypes.HAPPY_VILLAGER, lazyPos.getX() + x, lazyPos.getY() + y, lazyPos.getZ() + z, 0, 0, 0);
                                }
                    }
                } else this.handleFlight(player);
            }

            List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0), living -> living.isAlive() && living.hasEffect(MobEffects.POISON));
            for (LivingEntity poisoned : entities) entity.getPersistentData().putBoolean("RevivingPoisoned", true);

            BlockPos pos = player.blockPosition();
            if (player.level() instanceof ServerLevel world) {
                for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-3, -1, -3), pos.offset(3, 1, 3))) {
                    BlockState state = world.getBlockState(blockPos);
                    if (state.getBlock() instanceof CropBlock cropBlock) {
                        if (cropBlock.getMaxAge() > cropBlock.getAge(state) && random.nextInt(16) == 0) {
                            cropBlock.randomTick(state, world, blockPos, player.getRandom());
                            Vec3 center = blockPos.getCenter();
                            if (random.nextInt(12) == 0)
                                world.sendParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y, center.z, 1, 0.2, 0.2, 0.2, 0);
                        }
                    } else if (state.getBlock() instanceof StemBlock stemBlock) {
                        stemBlock.randomTick(state, world, blockPos, player.getRandom());
                        Vec3 center = blockPos.getCenter();
                        if (random.nextInt(12) == 0)
                            world.sendParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y, center.z, 1, 0.2, 0.2, 0.2, 0);
                    }
                }
            }
        }
        if (entity.tickCount % naturalRegenerationSpeed.getValue() == 0 && entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(Math.max(0.5F, entity.getMaxHealth() / 100.0F));
        }
    }

    private boolean hasPlantBy(Player player) {
        BlockPos blockPos = player.blockPosition();
        Iterable<BlockPos> posSet = BlockPos.betweenClosed(blockPos.offset(-5, -5, -5), blockPos.offset(5, 5, 5));
        if (player.getPersistentData().contains("RevivalFlightLazyPos")) {
            long data = player.getPersistentData().getLong("RevivalFlightLazyPos");
            BlockPos lazyPos = BlockPos.of(data);
            BlockState blockState = player.level().getBlockState(lazyPos);
            if (PLANT_SET.stream().anyMatch(blockState::is)) {
                if (lazyPos.distToCenterSqr(player.position()) < Math.pow(player.getEntityReach() + 1, 2)) return true;
            }
        }
        for (BlockPos pos : posSet) {
            BlockState blockState = player.level().getBlockState(pos);
            if (PLANT_SET.stream().anyMatch(blockState::is)) {
                if (pos.distToCenterSqr(player.position()) < Math.pow(player.getEntityReach() + 1, 2)) {
                    player.getPersistentData().putLong("RevivalFlightLazyPos", pos.asLong());
                    return true;
                }
            }
        }
        return false;
    }

    protected void handleFlight(Player player) {
        try {
            if (this.hasPlantBy(player)) {
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
                this.flyMap.put(player, 5);
            } else if (this.flyMap.get(player) > 1) {
                this.flyMap.put(player, this.flyMap.get(player) - 1);
            } else if (this.flyMap.get(player) == 1) {
                if (!player.isCreative() && !player.isSpectator()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
                player.getPersistentData().remove("RevivalFlightLazyPos");
                this.flyMap.put(player, 0);
            }
        } catch (NullPointerException exception) {
            exception.printStackTrace();
            player.getPersistentData().remove("RevivalFlightLazyPos");
            this.flyMap.put(player, 0);
        }
    }

    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            if (!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            player.getPersistentData().remove("RevivalFlightLazyPos");
            this.flyMap.put(player, 0);
        }
    }
}

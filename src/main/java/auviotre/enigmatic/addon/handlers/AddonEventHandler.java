package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.api.events.LivingCurseBoostEvent;
import auviotre.enigmatic.addon.contents.entities.goal.*;
import auviotre.enigmatic.addon.contents.items.*;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.gui.GUIUtils;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.*;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.aizistral.enigmaticlegacy.EnigmaticLegacy.PROXY;

@Mod.EventBusSubscriber(modid = EnigmaticAddons.MODID)
public class AddonEventHandler {
    public static final Map<Player, AABB> NIGHT_SCROLL_BOXES = new WeakHashMap<>();
    public static final List<ResourceKey<DamageType>> NEMESIS_LIST = List.of(DamageTypes.MOB_ATTACK, DamageTypes.GENERIC, DamageTypes.PLAYER_ATTACK);
    public static final Random RANDOM = new Random();

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onTooltipRendering(RenderTooltipEvent.@NotNull Color event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            if (item instanceof ItemBase && !ForgeRegistries.ITEMS.getKey(item).getNamespace().equals(EnigmaticLegacy.MODID)) {
                int background = GUIUtils.DEFAULT_BACKGROUND_COLOR;
                int borderStart = GUIUtils.DEFAULT_BORDER_COLOR_START;
                int borderEnd = GUIUtils.DEFAULT_BORDER_COLOR_END;

                if (item instanceof ICursed || item instanceof CursedRing) {
                    background = 0xF7101010;
                    borderStart = 0x50FF0C00;
                    borderEnd = borderStart;
                } else if (item == EnigmaticItems.COSMIC_SCROLL) {
                    background = 0xF0100010;
                    borderStart = 0xB0A800A8;
                    borderEnd = (borderStart & 0x3E3E3E) >> 1 | borderStart & 0xFF000000;
                }
                event.setBackground(background);
                event.setBorderStart(borderStart);
                event.setBorderEnd(borderEnd);
            }
        }
    }

    @SubscribeEvent
    public void onEntityAttacked(@NotNull LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof Shulker shulker && SuperAddonHandler.isCurseBoosted(shulker)) {
            if (shulker.isClosed()) {
                event.setCanceled(true);
                return;
            }
        }
        if (event.getEntity() instanceof Animal animal && event.getSource().getEntity() instanceof Player player) {
            if (SuperpositionHandler.hasItem(player, EnigmaticAddonItems.LIVING_ODE)) {
                if (EnigmaticItems.ANIMAL_GUIDEBOOK.isProtectedAnimal(animal)) {
                    event.setCanceled(true);

                    if (animal.getTarget() == player) {
                        event.setCanceled(false);
                    } else {
                        for (WrappedGoal goal : animal.targetSelector.getAvailableGoals()) {
                            if (goal.getGoal() instanceof TargetGoal targetGoal) {
                                if (targetGoal.targetMob == player) {
                                    event.setCanceled(false);
                                }
                            }
                        }
                    }
                    Brain<?> brain = animal.getBrain();
                    //noinspection ConstantValue
                    if (brain != null) {
                        try {
                            var memory = brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                                    ? brain.getMemory(MemoryModuleType.ATTACK_TARGET) : Optional.empty();
                            if (memory.isPresent() && memory.get() == player) {
                                event.setCanceled(false);
                            }
                        } catch (NullPointerException ignored) {
                        }
                    }
                }
            }
        }

        if (event.getSource().getEntity() instanceof Player player) {
            if (!SuperAddonHandler.isCurseBoosted(event.getEntity()) && SuperpositionHandler.isTheWorthyOne(player)) {
                SuperAddonHandler.setCurseBoosted(event.getEntity(), true, player);
            }
        }
    }

    @SubscribeEvent
    public void onLivingChangeTarget(@NotNull LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity target = event.getNewTarget();
        if (entity instanceof NeutralMob neutral && neutral instanceof Animal && target instanceof Player player && event.getTargetType() == LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) {
            if (SuperpositionHandler.isTheCursedOne(player) && (neutral.getTarget() == null || !neutral.getTarget().isAlive())) {
                if (neutral.getLastHurtByMob() != player && SuperpositionHandler.hasItem(player, EnigmaticAddonItems.LIVING_ODE)) {
                    event.setCanceled(true);
                }
            }
        }
        if (entity instanceof AbstractGolem golem && target instanceof Player player && event.getTargetType() == LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) {
            if (SuperpositionHandler.isTheCursedOne(player) && (golem.getTarget() == null || !golem.getTarget().isAlive())) {
                if (golem.getLastHurtByMob() != player && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.LOST_ENGINE)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(MobSpawnEvent.@NotNull FinalizeSpawn event) {
        if (event.getSpawnType() == MobSpawnType.NATURAL) {
            LivingEntity entity = event.getEntity();
            if (entity instanceof Phantom || entity.getVehicle() instanceof Phantom) {
                if (NIGHT_SCROLL_BOXES.values().stream().anyMatch(entity.getBoundingBox()::intersects)) {
                    event.setSpawnCancelled(true);
                    event.setResult(Event.Result.DENY);
                    event.setCanceled(true);
                }
            }
        }
        if (OmniconfigAddonHandler.ImmediatelyCurseBoost.getValue())
            SuperAddonHandler.setCurseBoosted(event.getEntity(), true, null);
    }

    @SubscribeEvent
    public void onPhantomsSpawn(@NotNull PlayerSpawnPhantomsEvent event) {
        Player player = event.getEntity();
        if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.NIGHT_SCROLL)) {
            event.setResult(Event.Result.DENY);
            event.setPhantomsToSpawn(0);
            return;
        }
        List<Entity> entities = player.level().getEntities(null, player.getBoundingBox().inflate(6.0));
        for (Entity entity : entities) {
            if (entity instanceof Player nearPlayer && SuperpositionHandler.hasCurio(nearPlayer, EnigmaticAddonItems.NIGHT_SCROLL)) {
                event.setResult(Event.Result.DENY);
                event.setPhantomsToSpawn(0);
                return;
            }
        }
    }

    @SubscribeEvent
    public void onTick(LivingEvent.@NotNull LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isAlive()) return;

        if (entity instanceof Phantom phantom) {
            if (NIGHT_SCROLL_BOXES.values().stream().anyMatch(phantom.getBoundingBox()::intersects)) {
                if (!phantom.hasEffect(MobEffects.WITHER)) {
                    phantom.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
                }
            }
        }

//        if (entity instanceof Slime slime) {
//            int size = slime.getSize();
//            List<ItemEntity> entities = slime.level().getEntitiesOfClass(ItemEntity.class, slime.getBoundingBox());
//            if (!entities.isEmpty() && size < 8) {
//                for (ItemEntity itemEntity : entities) {
//                    SimpleParticleType particleType = null;
//                    if (slime.getClass() == Slime.class && itemEntity.getItem().is(Items.SLIME_BALL)) {
//                        particleType = ParticleTypes.ITEM_SLIME;
//                    } else if (slime.getClass() == MagmaCube.class && itemEntity.getItem().is(Items.MAGMA_CREAM)) {
//                        particleType = ParticleTypes.FLAME;
//                    }
//                    if (particleType != null && !itemEntity.hasPickUpDelay()) {
//                        int count = itemEntity.getItem().getCount();
//                        if (count > 8) continue;
//                        if (count > 0) {
//                            count = Mth.clamp(count, 1, 8 - size);
//                            float health = (slime.getMaxHealth() - slime.getHealth()) / size * (size + count);
//                            slime.setSize(size + count, false);
//                            slime.setHealth(slime.getMaxHealth() - health);
//                        }
//                        itemEntity.kill();
//                    }
//                }
//            }
//        }

        int cooldown = entity.getPersistentData().getInt("CosmicPotion");
        if (cooldown > 0) {
            cooldown -= entity.getActiveEffects().isEmpty() ? 3 : 1;
            if (cooldown > 0) {
                event.getEntity().getPersistentData().putInt("CosmicPotion", cooldown);
            } else {
                event.getEntity().getPersistentData().remove("CosmicPotion");
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.@NotNull PlayerTickEvent event) {
        Player player = event.player;
        if (!player.isAlive() && event.phase != TickEvent.Phase.END) return;

        if (!player.level().isClientSide) {
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.NIGHT_SCROLL) && SuperpositionHandler.isTheCursedOne(player)) {
                NIGHT_SCROLL_BOXES.put(player, SuperAddonHandler.getBoundingBoxAroundEntity(player, 128, 360, 128));
            } else {
                NIGHT_SCROLL_BOXES.remove(player);
            }
        }

        if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.REVIVAL_LEAF)) {
            if (!player.getActiveEffects().isEmpty()) {
                for (MobEffectInstance effect : player.getActiveEffects()) {
                    if (player.tickCount % 4 == 0 && effect.duration > 0) {
                        effect.duration += 1;
                    }
                }
            }
        }

        if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.LOST_ENGINE)) {
            player.getCooldowns().tick();
            if (player.isInWater()) player.addDeltaMovement(new Vec3(0.0D, -0.006D, 0.0D));

            boolean spaceDown = Minecraft.getInstance().options.keyJump.isDown();
            if (spaceDown && player.getDeltaMovement().y > 0.24F && !player.level().getBlockState(player.blockPosition()).canOcclude()) {
                player.addDeltaMovement(new Vec3(0.0D, 0.025D, 0.0D));
                float width = player.getBbWidth();
                for (int i = 0; i < RANDOM.nextInt(3); i++) {
                    player.level().addParticle(ParticleTypes.CLOUD, player.getRandomX(width), player.getY() + RANDOM.nextFloat(0.2F), player.getRandomZ(width), 0, RANDOM.nextFloat(0.5F) * player.getDeltaMovement().y, 0);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFinalTarget(@NotNull LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isAlive()) return;

        if (!SuperAddonHandler.isCurseBoosted(entity) && event.getNewTarget() instanceof Player player) {
            if (SuperpositionHandler.isTheWorthyOne(player)) {
                SuperAddonHandler.setCurseBoosted(entity, true, player);
            }
        }
    }

    @SubscribeEvent
    public void onEntityHurt(@NotNull LivingHurtEvent event) {
        float amount = event.getAmount();
        if (amount >= Float.MAX_VALUE) return;
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        float damageBoost = 0F;

        if (event.getSource().getDirectEntity() instanceof Player player && event.getSource().is(EnigmaticAddonDamageTypes.FALSE_JUSTICE)) {
            if (SuperpositionHandler.hasCurio(player, EnigmaticItems.VOID_PEARL)) {
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.WITHER, VoidPearl.witheringTime.getValue(), VoidPearl.witheringLevel.getValue(), false, true));
            }

            if (player instanceof ServerPlayer) {
                if (player.getMainHandItem().is(EnigmaticItems.ENDER_SLAYER)) {
                    if (event.getEntity() instanceof ServerPlayer targetPlayer) {
                        targetPlayer.getCooldowns().addCooldown(Items.ENDER_PEARL, 400);
                        targetPlayer.getCooldowns().addCooldown(EnigmaticItems.RECALL_POTION, 400);
                        targetPlayer.getCooldowns().addCooldown(EnigmaticItems.TWISTED_MIRROR, 400);

                        if (SuperpositionHandler.hasCurio(targetPlayer, EnigmaticItems.EYE_OF_NEBULA) || SuperpositionHandler.hasCurio(targetPlayer, EnigmaticItems.THE_CUBE)) {
                            SuperpositionHandler.setSpellstoneCooldown(targetPlayer, 400);
                        }
                    }
                    if (event.getEntity() instanceof EnderMan || event.getEntity() instanceof Shulker) {
                        event.getEntity().getPersistentData().putInt("ELTeleportBlock", 400);
                    }
                }

                if (!player.getMainHandItem().isEmpty()) {
                    ItemStack mainHandItem = player.getMainHandItem();
                    if (mainHandItem.is(EnigmaticItems.THE_TWIST)) {
                        if (OmniconfigHandler.isBossOrPlayer(event.getEntity())) {
                            damageBoost += event.getAmount() * TheTwist.bossDamageBonus.getValue().asModifier(false);
                        }
                    } else if (mainHandItem.is(EnigmaticItems.THE_INFINITUM)) {
                        if (SuperpositionHandler.isTheWorthyOne(player)) {
                            if (OmniconfigHandler.isBossOrPlayer(event.getEntity())) {
                                damageBoost += event.getAmount() * TheInfinitum.bossDamageBonus.getValue().asModifier(false);
                            }
                        } else {
                            event.setCanceled(true);
                            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 3, false, true));
                            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 500, 3, false, true));
                            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 3, false, true));
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 3, false, true));
                            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 300, 3, false, true));
                            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 3, false, true));
                        }
                    } else if (mainHandItem.is(EnigmaticItems.ENDER_SLAYER)) {
                        if (EnigmaticItems.ENDER_SLAYER.isEndDweller(event.getEntity())) {
                            if (player.level().dimension().equals(PROXY.getEndKey())) {
                                if (event.getEntity() instanceof EnderMan && RegisteredMeleeAttack.getRegisteredAttackStregth(player) >= 1F) {
                                    event.setAmount((event.getAmount() + 100F) * 10F);
                                }
                                event.getEntity().getPersistentData().putBoolean("EnderSlayerVictim", true);
                            }

                            damageBoost += event.getAmount() * EnderSlayer.endDamageBonus.getValue().asModifier(false);
                        }
                    }
                }
            }
        }

        if (source.getEntity() instanceof Player player) {
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.CURSED_XP_SCROLL)) {
                ItemStack itemstack = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.CURSED_XP_SCROLL);
                damageBoost += (float) (event.getAmount() * (CursedXPScroll.getLevelModifier(itemstack) / 100.0 * CursedXPScroll.damageBoostLimit.getValue()));
            }
        }

        if (victim instanceof Player player) {
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.NIGHT_SCROLL) && NightScroll.isDark(player)) {
                damageBoost -= amount * NightScroll.abilityBoost.getValue().asModifier(false);
            }

            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.HELL_BLADE_CHARM)) {
                damageBoost += amount * 0.25F;
            }

            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.FORGOTTEN_ICE)) {
                if (source.getEntity() instanceof LivingEntity attacker && NEMESIS_LIST.stream().anyMatch(source::is)) {
                    if (attacker.canFreeze()) {
                        if (!attacker.level().isClientSide) {
                            ((ServerLevel) attacker.level()).sendParticles(ParticleTypes.SNOWFLAKE, attacker.getX(), attacker.getY(), attacker.getZ(), 20, attacker.getBbWidth() / 2, attacker.getBbHeight(), attacker.getBbWidth() / 2, 0.0D);
                        }
                        attacker.hurt(attacker.damageSources().source(DamageTypes.FREEZE, player), 2.0F);
                        attacker.setTicksFrozen(attacker.getTicksRequiredToFreeze());
                    }
                }
            }
        }

        if (source.getEntity() instanceof LivingEntity attacker) {
            if (SuperpositionHandler.hasCurio(attacker, EnigmaticAddonItems.FORGOTTEN_ICE) || attacker.hasEffect(EnigmaticAddonEffects.FROZEN_HEART_EFFECT)) {
                if (NEMESIS_LIST.stream().anyMatch(source::is)) {
                    if (victim.canFreeze()) victim.setTicksFrozen(victim.getTicksFrozen() + 60);
                }
                if (victim.isFullyFrozen())
                    damageBoost += amount * ForgottenIce.frostBoost.getValue().asModifier(false);
            }

            if (SuperpositionHandler.hasCurio(attacker, EnigmaticAddonItems.REVIVAL_LEAF)) {
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, RevivalLeaf.poisonTime.getValue(), RevivalLeaf.poisonLevel.getValue(), false, true), attacker);
            }

            if (attacker instanceof Vindicator vindicator) {
                boolean flag = vindicator.fallDistance > 0.0F && !vindicator.onGround() && !vindicator.onClimbable() && !vindicator.isInWater() && !vindicator.hasEffect(MobEffects.BLINDNESS) && !vindicator.isPassenger();
                if (flag) {
                    if (!victim.level().isClientSide) {
                        ((ServerLevel) victim.level()).getChunkSource().broadcastAndSend(attacker, new ClientboundAnimatePacket(victim, 4));
                    }
                    attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
                    damageBoost += amount * 0.5F;
                }
            }

            if (attacker instanceof Stray && victim.canFreeze())
                victim.setTicksFrozen(victim.getTicksFrozen() + 65 + Mth.floor(amount * 10));

            if (attacker instanceof MagmaCube) {
                victim.setSecondsOnFire(4);
            }
            if (attacker instanceof WitherSkeleton) {
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 2), attacker);
            }
        }

        if (damageBoost != 0) event.setAmount(amount + damageBoost);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityHurt(@NotNull LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        float amount = event.getAmount();
        if (victim instanceof Player player) {
            if (attacker instanceof Drowned drowned && SuperAddonHandler.isCurseBoosted(drowned)) {
                int airSupply = player.getAirSupply();
                int air = airSupply * 0.4 < 30 ? airSupply - 30 : Mth.floor(airSupply * 0.6);
                player.setAirSupply(Math.max(air, 0));
            }

            ItemCooldowns cooldowns = player.getCooldowns();
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.EARTH_PROMISE) && !cooldowns.isOnCooldown(EnigmaticAddonItems.EARTH_PROMISE)) {
                float damage = amount * EarthPromise.totalResistance.getValue().asModifierInverted();
                if (player.isAlive() && damage >= player.getHealth() * EarthPromise.abilityTriggerPercent.getValue().asModifier(false)) {
                    cooldowns.addCooldown(EnigmaticAddonItems.EARTH_PROMISE, EarthPromise.cooldown.getValue());
                    if (!player.level().isClientSide()) {
                        ((ServerLevel) player.level()).sendParticles(ParticleTypes.FLASH, player.getX(), player.getY(), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        ((ServerLevel) player.level()).sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(0.5F), player.getZ(), 36, 0.1D, 0.1D, 0.1D, 0.2D);
                        player.level().playSound(null, player, SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 5.0F, 1.5F);
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4));
                    event.setCanceled(true);
                } else {
                    event.setAmount(damage);
                }
            }
        }

        if (SuperpositionHandler.hasCurio(victim, EnigmaticAddonItems.QUARTZ_RING) && EnigmaticAddonItems.QUARTZ_RING.resistanccList.stream().anyMatch(event.getSource()::is)) {
            event.setAmount(amount * (1 - QuartzRing.magicResistance.getValue().asModifier(false)));
        }

        if (SuperpositionHandler.hasCurio(victim, EnigmaticAddonItems.LOST_ENGINE) && event.getSource().is(DamageTypes.LIGHTNING_BOLT)) {
            event.setAmount(amount * (victim.getRandom().nextInt(49) + 49) + victim.getMaxHealth());
        }

        if (attacker instanceof Player player && !player.level().isClientSide) {
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.HELL_BLADE_CHARM) && NEMESIS_LIST.stream().anyMatch(event.getSource()::is)) {
                float healthPer = victim.getHealth() * (SuperpositionHandler.isTheCursedOne(player) ? HellBladeCharm.killCursedThreshold.getValue().asModifier() : HellBladeCharm.killThreshold.getValue().asModifier());
                if (amount >= healthPer) {
                    player.heal((float) (victim.getHealth() * HellBladeCharm.healMultiplier.getValue()));
                    event.setAmount(amount * 10.0F);
                    player.level().playSound(player, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 2.0F);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFinalDamaged(@NotNull LivingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof Player player) {
            float lifesteal = 0.0F;

            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.NIGHT_SCROLL) && NightScroll.isDark(player)) {
                if (SuperpositionHandler.isTheCursedOne(player)) {
                    lifesteal += event.getAmount() * NightScroll.abilityBoost.getValue().asModifier(false);
                }
            }
            if (lifesteal > 0) player.heal(lifesteal);
        }
    }

    @SubscribeEvent
    public void onFinalDeath(@NotNull LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();

        if (entity instanceof Player player && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.CURSED_XP_SCROLL)) {
            ItemStack curioStack = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.CURSED_XP_SCROLL);
            ItemNBTHelper.setInt(curioStack, "XPStored", 0);
        }

        if (!OmniconfigAddonHandler.NearDeathAnger.getValue()) return;
        if (entity instanceof Mob && SuperAddonHandler.isCurseBoosted(entity) && sourceEntity instanceof LivingEntity target && target.isAlive()) {
            if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return;
            List<Entity> entities = entity.level().getEntities(entity, entity.getBoundingBox().inflate(16.0D));
            for (Entity near : entities) {
                if (near instanceof Mob mob && mob.getClass() == entity.getClass() && mob.getTarget() == null) {
                    if (mob.distanceToSqr(entity) < 12.0) {
                        mob.setTarget(target);
                    } else {
                        Vec3 viewVector = mob.getViewVector(0.0F);
                        Vec3 subtract = entity.position().subtract(mob.position());
                        if (subtract.dot(viewVector) > 0) {
                            mob.setTarget(target);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.@NotNull LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (SuperpositionHandler.hasCurio(entity, EnigmaticAddonItems.LOST_ENGINE)) {
            entity.addDeltaMovement(new Vec3(0.0, 0.12, 0.0));
            if (entity.isCrouching()) {
                float rot = entity.getYRot() * Mth.PI / 180.0F;
                float sin = -Mth.sin(rot) * 0.055F;
                float cos = Mth.cos(rot) * 0.055F;
                entity.addDeltaMovement(new Vec3(sin * 3F, 0.45, cos * 3F));
                for (int i = 0; i < 5; i++) {
                    float width = entity.getBbWidth();
                    entity.level().addParticle(ParticleTypes.CLOUD, entity.getRandomX(width), entity.getY() + RANDOM.nextFloat(width), entity.getRandomZ(0.5), sin, RANDOM.nextFloat(0.12F) + 0.05, cos);
                }
            }
        }
    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.LOST_ENGINE)) {
            event.setDamageModifier(event.getDamageModifier() + LostEngine.critModifier.getValue().asModifier());
        }
    }

    @SubscribeEvent
    public void onCursed(@NotNull LivingCurseBoostEvent event) {
        LivingEntity entity = event.getEntity();
        Player worthy = event.getTheWorthyOne();
        if (entity.level().isClientSide) return;
        String boost = "CurseAttributeBoost";

        if (entity.getClass() == Zombie.class) {
            if (entity.getMainHandItem().isEmpty() && entity.getRandom().nextInt(5) == 0) {
                entity.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TORCH, entity.getRandom().nextInt(4) + 3));
            } else if (entity.getMainHandItem().isEmpty() && entity.getRandom().nextInt(8) == 0) {
                entity.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WOODEN_AXE));
            }
        }
        if (entity.getClass() == Drowned.class && entity.getRandom().nextInt(100) <= 5) {
            if (entity.getMainHandItem().isEmpty()) {
                entity.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TRIDENT));
            }
        }

        if (entity instanceof IronGolem && worthy != null) {
            worthy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 4));
        }

        if (entity instanceof Vex vex && vex.getClass() == Vex.class) {
            vex.setItemSlot(EquipmentSlot.OFFHAND, vex.getItemBySlot(EquipmentSlot.MAINHAND));
            vex.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
            vex.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            vex.setDropChance(EquipmentSlot.CHEST, -1.0F);
            vex.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 1));
        }
        if (entity instanceof Blaze blaze) {
            blaze.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(boost, 2.0, AttributeModifier.Operation.ADDITION));
            blaze.getAttribute(Attributes.ATTACK_KNOCKBACK).addPermanentModifier(new AttributeModifier(boost, 1.0, AttributeModifier.Operation.ADDITION));
        }
        if (entity instanceof AbstractPiglin piglin) {
            piglin.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(boost, 2.0, AttributeModifier.Operation.ADDITION));
            piglin.getAttribute(Attributes.ARMOR).addPermanentModifier(new AttributeModifier(boost, 8.0, AttributeModifier.Operation.ADDITION));
            piglin.getAttribute(Attributes.ARMOR_TOUGHNESS).addPermanentModifier(new AttributeModifier(boost, 4.0, AttributeModifier.Operation.ADDITION));
        }
        if (entity instanceof MagmaCube magma) {
            magma.getAttribute(Attributes.ARMOR).addPermanentModifier(new AttributeModifier(boost, 3.0, AttributeModifier.Operation.ADDITION));
            magma.getAttribute(Attributes.ARMOR_TOUGHNESS).addPermanentModifier(new AttributeModifier(boost, 4.0, AttributeModifier.Operation.ADDITION));
        }
    }

    @SubscribeEvent
    public void onXPOrb(@NotNull PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (SuperpositionHandler.hasItem(player, EnigmaticAddonItems.CURSED_XP_SCROLL)) {
            ExperienceOrb orb = event.getOrb();
            orb.value = orb.getValue() / 5;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void miningStuff(PlayerEvent.@NotNull BreakSpeed event) {
        Player player = event.getEntity();
        float miningBoost = 1.0F;
        if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.EARTH_PROMISE)) {
            miningBoost += EarthPromise.breakSpeedBonus.getValue().asModifier();
        }

        event.setNewSpeed(event.getNewSpeed() * miningBoost);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingDrops(@NotNull LivingDropsEvent event) {
        DamageSource source = event.getSource();
        if (event.isRecentlyHit() && source != null && source.getEntity() instanceof Player player && SuperpositionHandler.isTheWorthyOne(player)) {
            LivingEntity killed = event.getEntity();
            BlockPos blockPos = killed.blockPosition();
            int lootingLevel = ForgeHooks.getLootingLevel(killed, player, source);
            if (killed.level().dimension() == EnigmaticLegacy.PROXY.getOverworldKey() && !killed.level().canSeeSky(blockPos)) {
                if (blockPos.getY() <= 0 && killed instanceof Monster && RANDOM.nextInt(1000) < 30 + lootingLevel * 15) {
                    ItemEntity itemEntity = new ItemEntity(killed.level(), killed.getX(), killed.getY(), killed.getZ(), new ItemStack(EnigmaticAddonItems.EARTH_HEART_FRAGMENT));
                    itemEntity.setPickUpDelay(10);
                    event.getDrops().add(itemEntity);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLootTablesLoaded(LootTableLoadEvent event) {
        if (!OmniconfigHandler.customDungeonLootEnabled.getValue()) return;

        if (SuperAddonHandler.getIceDungeons().contains(event.getName())) {
            LootPool poolSpellstones = SuperpositionHandler.constructLootPool("addon_spellstones", -3F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.FORGOTTEN_ICE, 100));

            LootTable modified = event.getTable();
            modified.addPool(poolSpellstones);
            event.setTable(modified);
        }
        if (SuperAddonHandler.getJungleDungeons().contains(event.getName())) {
            LootPool poolSpellstones = SuperpositionHandler.constructLootPool("addon_spellstones", -3F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.REVIVAL_LEAF, 100));

            LootTable modified = event.getTable();
            modified.addPool(poolSpellstones);
            event.setTable(modified);
        }
        if (SuperAddonHandler.getEngineDungeons().contains(event.getName())) {
            LootPool poolSpellstones = SuperpositionHandler.constructLootPool("addon_spellstones", -9F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.LOST_ENGINE, 100));

            LootTable modified = event.getTable();
            modified.addPool(poolSpellstones);
            event.setTable(modified);
        }
        if (SuperAddonHandler.getNetherDungeons().contains(event.getName())) {
            LootPool poolCharms = SuperpositionHandler.constructLootPool("addon_charms", -1F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.HELL_BLADE_CHARM, 100));

            LootTable modified = event.getTable();
            modified.addPool(poolCharms);
            event.setTable(modified);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLootTablesPreLoaded(@NotNull LootTableLoadEvent event) {
        LootTable modified = event.getTable();
        if (SuperpositionHandler.getOverworldDungeons().contains(event.getName())) {
            LootPool earth_bonus = SuperpositionHandler.constructLootPool("earth_bonus", -1F, 1F,
                    SuperpositionHandler.getWaterDungeons().contains(event.getName()) || event.getName().equals(BuiltInLootTables.PILLAGER_OUTPOST) ? null : SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.EARTH_HEART_FRAGMENT, 50, 1, 2)
            );
            modified.addPool(earth_bonus);
            event.setTable(modified);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(@NotNull EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Predicate<WrappedGoal> meleeOrBow = ((goal) -> goal.getGoal() instanceof MeleeAttackGoal || goal.getGoal() instanceof RangedCrossbowAttackGoal<?>);
        int priority;
        if (entity instanceof Spider spider) {
            priority = getGoalPriority(spider, meleeOrBow);
            if (priority >= 0)
                spider.goalSelector.addGoal(priority, new SpiderRangedAttackGoal(spider, 0.5F, 30, 8.0F));
        }
        if (entity instanceof Pillager pillager) {
            priority = getGoalPriority(pillager, (goal) -> goal.getGoal() instanceof RangedCrossbowAttackGoal) - 1;
            if (priority >= 0)
                pillager.goalSelector.addGoal(priority, new CrossbowBlazeAttackGoal<>(pillager, 1.05, 8.5F));
        }
        if (entity instanceof AbstractIllager illager) {
            priority = getGoalPriority(illager, meleeOrBow) - 1;
            if (priority >= 0) illager.goalSelector.addGoal(priority, new LeapAttackGoal(illager, 0.5D));
        }
        if (entity instanceof Zombie zombie) {
            priority = getGoalPriority(zombie, meleeOrBow) - 1;
            if (priority >= 0) zombie.goalSelector.addGoal(priority, new LeapAttackGoal(zombie, 0.36D));
        }
        if (entity instanceof AbstractSkeleton skeleton) {
            priority = getGoalPriority(skeleton, meleeOrBow);
            if (priority >= 0)
                skeleton.goalSelector.addGoal(priority, new SkeletonMeleeAttackGoal(skeleton));
        }
        if (entity instanceof Ghast ghast) {
            ghast.goalSelector.addGoal(7, new GhastMultishotGoal(ghast));
        }
        if (entity instanceof Animal animal) {
            if (animal instanceof Cow || animal instanceof Pig || animal instanceof Sheep || animal instanceof Chicken || animal instanceof Rabbit) {
                Predicate<LivingEntity> cursedPlayer = (player) -> SuperpositionHandler.isTheWorthyOne((Player) player) && !SuperpositionHandler.hasItem((Player) player, EnigmaticAddonItems.LIVING_ODE);
                animal.goalSelector.addGoal(2, new AvoidEntityGoal<>(animal, Player.class, 6.0F, 1.25, 1.25, cursedPlayer));
            }
        }
        if (entity instanceof Vex vex) {
            if (vex.getOwner() != null && SuperAddonHandler.isCurseBoosted(vex.getOwner())) {
                SuperAddonHandler.setCurseBoosted(vex, true, null);
            }
        }
    }

    @SubscribeEvent
    public void onWandererTradesEvent(@NotNull WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();
        rareTrades.add((trader, rand) -> new MerchantOffer(new ItemStack(EnigmaticAddonItems.EARTH_HEART_FRAGMENT), new ItemStack(Items.EMERALD, 20), new ItemStack(EnigmaticItems.EARTH_HEART), 1, 5, 0.2F));
        rareTrades.add((trader, rand) -> new MerchantOffer(new ItemStack(Items.EMERALD, 4), new ItemStack(EnigmaticAddonItems.EARTH_HEART_FRAGMENT), 2, 5, 0.25F));
    }

    public static int getGoalPriority(@NotNull Mob mob, Predicate<WrappedGoal> filter) {
        return mob.goalSelector.getAvailableGoals().stream().filter(filter).findFirst().map(WrappedGoal::getPriority).orElse(-1);
    }
}

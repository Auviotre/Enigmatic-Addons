package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.api.events.LivingCurseBoostEvent;
import auviotre.enigmatic.addon.contents.entities.goal.*;
import auviotre.enigmatic.addon.contents.items.*;
import auviotre.enigmatic.addon.contents.objects.bookbag.AntiqueBagCapability;
import auviotre.enigmatic.addon.contents.objects.bookbag.IAntiqueBagHandler;
import auviotre.enigmatic.addon.packets.PacketEmptyLeftClick;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEnchantments;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.*;
import com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
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
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
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

import static com.aizistral.enigmaticlegacy.EnigmaticLegacy.PROXY;

@Mod.EventBusSubscriber(modid = EnigmaticAddons.MODID)
public class AddonEventHandler {
    public static final Map<Player, AABB> NIGHT_SCROLL_BOXES = new WeakHashMap<>();
    public static final List<ResourceKey<DamageType>> NEMESIS_LIST = List.of(DamageTypes.MOB_ATTACK, DamageTypes.GENERIC, DamageTypes.PLAYER_ATTACK);
    public static final Multimap<Player, Item> POSTMORTAL_POSSESSIONS = ArrayListMultimap.create();
    public static final Random RANDOM = new Random();
    public static final UUID UUID_ATTACK = UUID.fromString("73B52DA4-C79F-48AF-87BB-4B9C037FED9F");
    public static final UUID UUID_ARMOR = UUID.fromString("2F35F0A9-794F-448C-921D-EBCC2CA951D3");
    public static final UUID UUID_ATTACK_KB = UUID.fromString("73B52DA4-C79F-48AF-87BB-4B9C037FED9F");
    public static final UUID UUID_ARMOR_TH = UUID.fromString("2F35F0A9-794F-448C-921D-EBCC2CA951D3");

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
    public void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity target = event.getNewTarget();
        if (entity instanceof Targeting targetedEntity && target instanceof Player player && event.getTargetType() == LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) {
            if (entity.getLastHurtByMob() != player && (targetedEntity.getTarget() == null || !targetedEntity.getTarget().isAlive())) {
                if (entity instanceof NeutralMob neutral && neutral instanceof Animal && SuperpositionHandler.isTheCursedOne(player) && SuperpositionHandler.hasItem(player, EnigmaticAddonItems.LIVING_ODE)) {
                    event.setCanceled(true);
                }
                if (entity instanceof AbstractGolem && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.LOST_ENGINE)) {
                    event.setCanceled(true);
                }
                if (LostEngine.golemList.contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())) && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.LOST_ENGINE)) {
                    event.setCanceled(true);
                }
            }
        }
    }

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

    /* 47.1 Forge Back-Compat
    @SubscribeEvent
    public void onPhantomsSpawn(PlayerSpawnPhantomsEvent event) {
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
    */

    @SubscribeEvent
    public void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isAlive()) return;

        if (entity instanceof Phantom phantom) {
            if (NIGHT_SCROLL_BOXES.values().stream().anyMatch(phantom.getBoundingBox()::intersects)) {
                if (!phantom.hasEffect(MobEffects.WITHER)) {
                    phantom.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
                }
            }
        }

        if (entity instanceof TamableAnimal pet && pet.isTame() && pet.getOwner() instanceof Player owner && SuperAddonHandler.isOKOne(owner)) {
            if (!pet.level().isClientSide && owner.level() == pet.level() && owner.distanceTo(pet) <= HunterGuidebook.effectiveDistance.getValue()) {
                if (SuperpositionHandler.hasItem(owner, EnigmaticAddonItems.SANGUINARY_HANDBOOK) && SuperpositionHandler.hasCurio(owner, EnigmaticItems.BERSERK_CHARM)) {
                    pet.getAttributes().addTransientAttributeModifiers(SanguinaryHandbook.createAttributeMap(owner));
                } else {
                    pet.getAttributes().removeAttributeModifiers(SanguinaryHandbook.createAttributeMap(owner));
                }
            }
        }
        // CosmicPotion
        CompoundTag data = entity.getPersistentData();
        int cooldown = data.getInt("CosmicPotion");
        if (cooldown > 0) {
            cooldown -= entity.getActiveEffects().isEmpty() ? 3 : 1;
            if (cooldown > 0) data.putInt("CosmicPotion", cooldown);
            else data.remove("CosmicPotion");
        } else data.remove("CosmicPotion");
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
                List<Entity> entities = entity.level().getEntities(entity, entity.getBoundingBox().inflate(2));
                boolean flag = true;
                for (Entity target : entities) {
                    if (target instanceof Player player && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.THUNDER_SCROLL)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(entity.level());
                    if (lightningbolt != null) {
                        lightningbolt.moveTo(Vec3.atBottomCenterOf(entity.blockPosition()));
                        lightningbolt.setSilent(entity.getRandom().nextBoolean());
                        lightningbolt.setDamage(lightningbolt.getDamage() * electric / 600.0F);
                        entity.level().addFreshEntity(lightningbolt);
                    }
                    data.putInt("Electric", (electric - 1200) / 2 + 100);
                }
            } else data.putInt("Electric", electric - 1);
        } else data.remove("Electric");

        if (data.contains(BlessRing.CURSED_SPAWN)) data.remove(BlessRing.CURSED_SPAWN);
        if (data.contains(BlessRing.BLESS_SPAWN)) data.remove(BlessRing.BLESS_SPAWN);

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
            if (player.tickCount % 2 == 0) player.getCooldowns().tick();
            if (player.isInWater()) player.addDeltaMovement(new Vec3(0.0D, -0.0064D, 0.0D));

            if (event.side == LogicalSide.CLIENT) {
                boolean spaceDown = Minecraft.getInstance().options.keyJump.isDown();
                if (spaceDown && player.getDeltaMovement().y > 0.225F && !player.level().getBlockState(player.blockPosition()).canOcclude()) {
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFirstHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.ETHERIUM_CORE)) {
            event.setAmount(event.getAmount() + player.getPersistentData().getFloat("EtheriumCounterattack"));
            player.getPersistentData().remove("EtheriumCounterattack");
        }

        if (event.getEntity() instanceof Player player && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.ETHERIUM_CORE)) {
            float counterattack = (float) Math.min(event.getAmount() * EtheriumCore.damageConversion.getValue().asModifier() + player.getPersistentData().getFloat("EtheriumCounterattack"), EtheriumCore.damageConversionMax.getValue());
            player.getPersistentData().putFloat("EtheriumCounterattack", counterattack);
        }
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        float amount = event.getAmount();
        if (event.getAmount() >= Float.MAX_VALUE) return;
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

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
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.CURSED_XP_SCROLL)) {
                ItemStack itemstack = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.CURSED_XP_SCROLL);
                ModifyDamageBaseOne(event, (CursedXPScroll.getLevelModifier(itemstack) / 100.0 * CursedXPScroll.damageBoostLimit.getValue()));
            }

            if (!SuperAddonHandler.findBookInBag(player, EnigmaticItems.THE_ACKNOWLEDGMENT).isEmpty() || !SuperAddonHandler.findBookInBag(player, EnigmaticItems.THE_TWIST).isEmpty()) {
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

            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.THUNDER_SCROLL)) {
                if (player.getMainHandItem().canPerformAction(ToolActions.SWORD_SWEEP)) {
                    int electric = victim.getPersistentData().getInt("Electric");
                    victim.getPersistentData().putInt("Electric", electric + 60 + player.getRandom().nextInt(80) + (int) (event.getAmount() * 10));
                }
                event.setAmount(ThunderScroll.modify(victim, event.getAmount()));
            }

            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.HELL_BLADE_CHARM)) {
                float damageMultiplier = 1.0F;
                if (SuperpositionHandler.hasCurio(player, EnigmaticItems.BERSERK_CHARM))
                    damageMultiplier += (SuperpositionHandler.getMissingHealthPool(player) * (float) BerserkEmblem.attackDamage.getValue() / 5);
                ModifyDamageBaseOne(event, (NEMESIS_LIST.stream().anyMatch(source::is) ? 1.0F : 0.5F) * damageMultiplier);
            }

            if (SuperAddonHandler.isTheBlessedOne(player)) {
                ModifyDamageBaseOne(event, BlessRing.damageBoost.getValue().asModifier(false));
            }
        }

        if (victim instanceof Player player) {
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.NIGHT_SCROLL) && NightScroll.isDark(player)) {
                ModifyDamageBaseOne(event, -NightScroll.abilityBoost.getValue().asModifier(false));
            }

            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.HELL_BLADE_CHARM)) {
                ModifyDamageBaseOne(event, 0.1F);
                if (SuperpositionHandler.hasCurio(player, EnigmaticItems.BERSERK_CHARM)) {
                    float resistance = SuperpositionHandler.getMissingHealthPool(player) * (float) BerserkEmblem.damageResistance.getValue();
                    event.setAmount(event.getAmount() * (1.0F - resistance / 2) / (1.0F - resistance));
                }
            }

            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.BLESS_RING)) {
                event.setAmount(event.getAmount() * (1 - BlessRing.damageResistance.getValue().asModifier()));
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
                    ModifyDamageBaseOne(event, ForgottenIce.frostBoost.getValue().asModifier(false));
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
                    ModifyDamageBaseOne(event, 0.5);
                }
            }

            if (attacker instanceof Stray && victim.canFreeze()) {
                victim.setTicksFrozen(victim.getTicksFrozen() + 65 + Mth.floor(amount * 10));
            }

            if (attacker instanceof MagmaCube) {
                victim.setSecondsOnFire(4);
            }

            if (attacker instanceof WitherSkeleton) {
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 2), attacker);
            }

            if (attacker instanceof TamableAnimal pet && pet.isTame() && pet.getOwner() instanceof Player owner) {
                if (SuperAddonHandler.isOKOne(owner) && SuperpositionHandler.hasItem(owner, EnigmaticAddonItems.SANGUINARY_HANDBOOK)) {
                    if (owner.level() == pet.level() && owner.distanceTo(pet) <= HunterGuidebook.effectiveDistance.getValue()) {
                        double damageMultiplier = SanguinaryHandbook.DamageMultiplier.getValue();
                        if (SuperpositionHandler.hasCurio(owner, EnigmaticItems.BERSERK_CHARM)) {
                            damageMultiplier += 0.5F * (SuperpositionHandler.getMissingHealthPool(owner) * (float) BerserkEmblem.attackDamage.getValue());
                        }
                        if (SuperpositionHandler.hasCurio(owner, EnigmaticItems.CURSED_SCROLL)) {
                            damageMultiplier += 0.75F * (SuperpositionHandler.getCurseAmount(owner) * CursedScroll.damageBoost.getValue().asModifier());
                        }
                        ModifyDamageBaseOne(event, damageMultiplier);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (victim instanceof Player player) {
            if (attacker instanceof Drowned drowned && SuperAddonHandler.isCurseBoosted(drowned)) {
                int airSupply = player.getAirSupply();
                int air = airSupply * 0.4 < 30 ? airSupply - 30 : Mth.floor(airSupply * 0.6);
                player.setAirSupply(Math.max(air, 0));
            }

            ItemCooldowns cooldowns = player.getCooldowns();
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.EARTH_PROMISE) && !cooldowns.isOnCooldown(EnigmaticAddonItems.EARTH_PROMISE)) {
                float damage = event.getAmount() * EarthPromise.totalResistance.getValue().asModifierInverted();
                if (player.isAlive() && damage >= player.getHealth() * EarthPromise.abilityTriggerPercent.getValue().asModifier(false)) {
                    cooldowns.addCooldown(EnigmaticAddonItems.EARTH_PROMISE, EarthPromise.cooldown.getValue());
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

        if (SuperpositionHandler.hasCurio(victim, EnigmaticAddonItems.QUARTZ_RING) && EnigmaticAddonItems.QUARTZ_RING.resistanccList.stream().anyMatch(event.getSource()::is)) {
            event.setAmount(event.getAmount() * (1 - QuartzRing.magicResistance.getValue().asModifier(false)));
        }

        if (SuperpositionHandler.hasCurio(victim, EnigmaticAddonItems.LOST_ENGINE) && event.getSource().is(DamageTypes.LIGHTNING_BOLT)) {
            event.setAmount(event.getAmount() * (victim.getRandom().nextInt(5) + 5) + victim.getMaxHealth());
        }

        if (attacker instanceof Player player && !player.level().isClientSide) {
            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.HELL_BLADE_CHARM) && NEMESIS_LIST.stream().anyMatch(event.getSource()::is)) {
                float healthPer = victim.getHealth() * (SuperpositionHandler.isTheCursedOne(player) ? HellBladeCharm.killCursedThreshold.getValue().asModifier() : HellBladeCharm.killThreshold.getValue().asModifier());
                if (event.getAmount() >= healthPer) {
                    player.heal((float) (victim.getHealth() * HellBladeCharm.healMultiplier.getValue()));
                    event.setAmount(event.getAmount() * 10.0F);
                    player.level().playSound(player, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 2.0F);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFinalDamage(LivingDamageEvent event) {
        float amount = event.getAmount();
        if (event.getSource().getDirectEntity() instanceof Player player) {
            float lifesteal = 0.0F;

            if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.NIGHT_SCROLL) && NightScroll.isDark(player)) {
                if (SuperpositionHandler.isTheCursedOne(player)) {
                    lifesteal += amount * NightScroll.abilityBoost.getValue().asModifier(false);
                }
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
    public void onFinalDeath(LivingDeathEvent event) {
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

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onConfirmedDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.isCanceled()) {
                POSTMORTAL_POSSESSIONS.removeAll(player);
                return;
            }

            if (SuperpositionHandler.hasItem(player, EnigmaticAddonItems.BLESS_STONE) && !SuperpositionHandler.hasItem(player, EnigmaticItems.CURSED_STONE) && SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_RING)) {
                POSTMORTAL_POSSESSIONS.put(player, EnigmaticAddonItems.BLESS_STONE);
                for (List<ItemStack> list : player.getInventory().compartments) {
                    for (ItemStack itemstack : list) {
                        if (!itemstack.isEmpty() && itemstack.getItem() == EnigmaticAddonItems.BLESS_STONE) {
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

    @SubscribeEvent
    public void onCurioDrops(DropRulesEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (POSTMORTAL_POSSESSIONS.containsKey(player) && POSTMORTAL_POSSESSIONS.containsEntry(player, EnigmaticAddonItems.BLESS_STONE)) {
                boolean confirm = true;

                if (confirm) {
                    event.addOverride(stack -> stack != null && (stack.is(EnigmaticItems.CURSED_RING) || stack.is(EnigmaticItems.DESOLATION_RING)), ICurio.DropRule.DESTROY);
                    CuriosApi.getCuriosInventory(player).ifPresent((handler) -> {
                        IItemHandlerModifiable curios = handler.getEquippedCurios();
                        for (int i = 0; i < handler.getSlots() - 1; ++i) {
                            if (curios.getStackInSlot(i) != null && curios.getStackInSlot(i).getItem() == EnigmaticItems.CURSED_RING) {
                                curios.setStackInSlot(i, EnigmaticAddonItems.BLESS_RING.getDefaultInstance());
                            }
                        }
                    });
                    SuperpositionHandler.destroyCurio(player, EnigmaticItems.DESOLATION_RING);

                    player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0F, 0.5F);
                    SuperpositionHandler.setPersistentBoolean(player, "DestroyedCursedRing", true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (SuperpositionHandler.hasCurio(entity, EnigmaticAddonItems.LOST_ENGINE)) {
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
        if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.LOST_ENGINE)) {
            event.setDamageModifier(event.getDamageModifier() + LostEngine.critModifier.getValue().asModifier());
        }
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        boolean flag = player.onGround() && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.THUNDER_SCROLL);
        if (flag && player.getMainHandItem().canPerformAction(ToolActions.SWORD_SWEEP) && !player.getCooldowns().isOnCooldown(EnigmaticAddonItems.THUNDER_SCROLL)) {
            EnigmaticAddons.packetInstance.send(PacketDistributor.SERVER.noArg(), new PacketEmptyLeftClick(true));
        }
    }

    @SubscribeEvent
    public void onCursed(LivingCurseBoostEvent event) {
        if (!OmniconfigAddonHandler.EnableCurseBoost.getValue()) return;
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
            blaze.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(UUID_ATTACK, boost, 2.0, AttributeModifier.Operation.ADDITION));
            blaze.getAttribute(Attributes.ATTACK_KNOCKBACK).addPermanentModifier(new AttributeModifier(UUID_ATTACK_KB, boost, 1.0, AttributeModifier.Operation.ADDITION));
        }
        if (entity instanceof AbstractPiglin piglin) {
            piglin.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(UUID_ATTACK, boost, 2.0, AttributeModifier.Operation.ADDITION));
            piglin.getAttribute(Attributes.ARMOR).addPermanentModifier(new AttributeModifier(UUID_ARMOR, boost, 8.0, AttributeModifier.Operation.ADDITION));
            piglin.getAttribute(Attributes.ARMOR_TOUGHNESS).addPermanentModifier(new AttributeModifier(UUID_ARMOR_TH, boost, 4.0, AttributeModifier.Operation.ADDITION));
        }
        if (entity instanceof MagmaCube magma) {
            magma.getAttribute(Attributes.ARMOR).addPermanentModifier(new AttributeModifier(UUID_ARMOR, boost, 3.0, AttributeModifier.Operation.ADDITION));
            magma.getAttribute(Attributes.ARMOR_TOUGHNESS).addPermanentModifier(new AttributeModifier(UUID_ARMOR_TH, boost, 4.0, AttributeModifier.Operation.ADDITION));
        }
    }

    @SubscribeEvent
    public void onXPOrb(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (SuperpositionHandler.hasItem(player, EnigmaticAddonItems.CURSED_XP_SCROLL)) {
            ExperienceOrb orb = event.getOrb();
            orb.value = orb.getValue() / 5;
        }
    }

    @SubscribeEvent
    public void miningStuff(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        float miningModifier = 0.0F;
        if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.EARTH_PROMISE)) {
            miningModifier += EarthPromise.breakSpeedBonus.getValue().asModifier();
        }

        event.setNewSpeed(event.getOriginalSpeed() * miningModifier + event.getNewSpeed());

        if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.ADVENTURE_CHARM) && !player.isCrouching()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() != null && !event.getEntity().level().isClientSide) {
            if (event.getInventory().countItem(EnigmaticAddonItems.BLESS_AMPLIFIER) == 1 || event.getCrafting().getItem() == EnigmaticAddonItems.BLESS_STONE) {
                event.getEntity().level().playSound(null, event.getEntity().blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, (float) (0.9F + (Math.random() * 0.1F)));
            }
        }
    }

    @SubscribeEvent
    public void onAnvilChange(AnvilUpdateEvent event) {
        Player player = event.getPlayer();
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!left.isEmpty() && !right.isEmpty()) {
            if (player != null && EnigmaticAddonItems.FORGER_GEM.isPresent(player)) {
                boolean check = left.isDamageableItem();
                if (check && ForgerGem.strictUnbreakableForge.getValue()) {
                    check = left.isRepairable() && (left.getItem() instanceof TieredItem || left.getItem() instanceof ArmorItem);
                }
                if (left.is(right.getItem()) && check && left.getDamageValue() == 0 && right.getDamageValue() == 0) {
                    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(left);
                    Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(right);
                    boolean flag2 = false;
                    boolean flag3 = false;
                    Iterator<Enchantment> enchantments1 = map1.keySet().iterator();

                    loop:
                    while (true) {
                        Enchantment enchantment1;
                        do {
                            if (!enchantments1.hasNext()) {
                                if (flag3 && !flag2) {
                                    return;
                                }
                                break loop;
                            }
                            enchantment1 = enchantments1.next();
                        } while (enchantment1 == null);

                        int level = map.getOrDefault(enchantment1, 0);
                        int level1 = map1.get(enchantment1);
                        boolean flag1 = enchantment1.canEnchant(left);
                        if (player.getAbilities().instabuild) {
                            flag1 = true;
                        }

                        for (Enchantment enchantment : map.keySet()) {
                            if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment)) {
                                flag1 = false;
                            }
                        }

                        if (!flag1) {
                            flag3 = true;
                        } else {
                            flag2 = true;
                            map.put(enchantment1, Math.min(level == level1 ? level1 + 1 : Math.max(level1, level), enchantment1.getMaxLevel()));
                        }
                    }
                    ItemStack copy = left.copy();
                    int cost = 30;
                    if (map.containsKey(Enchantments.UNBREAKING)) {
                        cost -= map.get(Enchantments.UNBREAKING);
                        map.remove(Enchantments.UNBREAKING);
                    }
                    if (map.containsKey(Enchantments.MENDING)) {
                        cost -= map.get(Enchantments.MENDING) * 3;
                        map.remove(Enchantments.MENDING);
                    }
                    EnchantmentHelper.setEnchantments(map, copy);
                    copy.addTagElement("Unbreakable", ByteTag.valueOf(true));
                    copy.setRepairCost(20);
                    event.setOutput(copy);
                    event.setCost(cost);
                }
            }
        }
    }

    @SubscribeEvent
    public void onAnvilRepair(AnvilRepairEvent event) {
        Player player = event.getEntity();
        if (player != null && !player.level().isClientSide && SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.FORGER_GEM)) {
            event.setBreakChance(event.getBreakChance() / 6);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingDrops(LivingDropsEvent event) {
        DamageSource source = event.getSource();
        if (event.isRecentlyHit() && source != null && source.getEntity() instanceof Player player && SuperAddonHandler.isOKOne(player)) {
            LivingEntity killed = event.getEntity();
            BlockPos blockPos = killed.blockPosition();
            int lootingLevel = ForgeHooks.getLootingLevel(killed, player, source);
            if (killed.level().dimension() == EnigmaticLegacy.PROXY.getOverworldKey() && !killed.level().canSeeSky(blockPos)) {
                if (blockPos.getY() <= 0 && killed instanceof Monster && RANDOM.nextInt(1000) < 30 + lootingLevel * 15) {
                    this.addDrop(event, new ItemStack(EnigmaticAddonItems.EARTH_HEART_FRAGMENT));
                }
            }

            if (!CursedRing.enableSpecialDrops.getValue())
                return;

            this.addEntityDropWithChance(event, Ghast.class, new ItemStack(EnigmaticAddonItems.ICHOR_DROPLET), 40);

            if (SuperAddonHandler.isTheBlessedOne(player)) {
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
                            new ItemStack(EnigmaticItems.GUARDIAN_HEART, 1),
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
                } else if (killed instanceof WitherBoss) {
                    this.addDrop(event, this.getRandomSizeStack(EnigmaticItems.EVIL_ESSENCE, 1, 4));
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
            LootPool poolSpellstones = SuperpositionHandler.constructLootPool("addon_spellstones", -4F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.REVIVAL_LEAF, 100));

            LootTable modified = event.getTable();
            modified.addPool(poolSpellstones);
            event.setTable(modified);
        }
        if (SuperAddonHandler.getEngineDungeons().contains(event.getName())) {
            LootPool poolSpellstones = SuperpositionHandler.constructLootPool("addon_spellstones", -15F, 1F,
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
        if (SuperpositionHandler.getOverworldDungeons().contains(event.getName())) {
            LootPool antiqueLegacy = SuperpositionHandler.constructLootPool("antique_legacy", -8F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.ANTIQUE_BAG, 25),
                    SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.VOID_TOME, 45),
                    SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.FORGER_GEM, 30));

            LootTable modified = event.getTable();
            modified.addPool(antiqueLegacy);
            event.setTable(modified);
        }
        if (SuperpositionHandler.getNetherDungeons().contains(event.getName())) {
            LootPool ichor = SuperpositionHandler.constructLootPool("ichor", -7F, 1F,
                    SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.ICHOR_DROPLET, 100, 1, 2));

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
                    SuperpositionHandler.getWaterDungeons().contains(event.getName()) || event.getName().equals(BuiltInLootTables.PILLAGER_OUTPOST) ? null : SuperAddonHandler.createOptionalLootEntry(EnigmaticAddonItems.EARTH_HEART_FRAGMENT, 50, 1, 2)
            );
            modified.addPool(earth_bonus);
            event.setTable(modified);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!OmniconfigAddonHandler.EnableCurseBoost.getValue()) return;
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
            if (animal instanceof Cow || animal instanceof Pig || animal instanceof Sheep || animal instanceof Chicken) {
                animal.goalSelector.addGoal(5, new AvoidTheWorthyGoal(animal, 6.0F, 1.25, 1.25));
            }
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
        rareTrades.add((trader, rand) -> new MerchantOffer(new ItemStack(EnigmaticAddonItems.EARTH_HEART_FRAGMENT), new ItemStack(Items.EMERALD, 20), new ItemStack(EnigmaticItems.EARTH_HEART), 1, 5, 0.2F));
        rareTrades.add((trader, rand) -> new MerchantOffer(new ItemStack(Items.EMERALD, 4), new ItemStack(EnigmaticAddonItems.EARTH_HEART_FRAGMENT), 2, 5, 0.25F));
    }

    public static int getGoalPriority(Mob mob, Predicate<WrappedGoal> filter) {
        return mob.goalSelector.getAvailableGoals().stream().filter(filter).findFirst().map(WrappedGoal::getPriority).orElse(-1);
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
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        Player original = event.getOriginal();
        original.revive();
        if (SuperpositionHandler.isTheCursedOne(original)) player.getPersistentData().putBoolean(BlessRing.CURSED_SPAWN, true);
        if (SuperAddonHandler.isTheBlessedOne(original)) player.getPersistentData().putBoolean(BlessRing.BLESS_SPAWN, true);
        LazyOptional<IAntiqueBagHandler> oldHandler = SuperAddonHandler.getCapability(original, AntiqueBagCapability.INVENTORY);
        LazyOptional<IAntiqueBagHandler> newHandler = SuperAddonHandler.getCapability(player, AntiqueBagCapability.INVENTORY);
        oldHandler.ifPresent(oldBag -> newHandler.ifPresent(newBag -> newBag.readTag(oldBag.writeTag())));
    }

    @SubscribeEvent
    public void onLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (SuperpositionHandler.isTheCursedOne(event.getEntity())) {
            event.getEntity().getPersistentData().putBoolean(BlessRing.CURSED_SPAWN, true);
        }
        if (SuperAddonHandler.isTheBlessedOne(event.getEntity())) {
            event.getEntity().getPersistentData().putBoolean(BlessRing.BLESS_SPAWN, true);
        }
    }
}

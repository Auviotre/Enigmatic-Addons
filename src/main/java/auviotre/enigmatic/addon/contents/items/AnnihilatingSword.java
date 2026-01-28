package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.entities.AbyssProjectile;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.packets.clients.PacketPlaySound;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.IEldritch;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.*;

public class AnnihilatingSword extends SwordItem implements IEldritch {
    public static Omniconfig.PerhapsParameter onlyMainHandModifier;
    public static Omniconfig.PerhapsParameter damageEnchantmentPerModifier;
    public static Omniconfig.PerhapsParameter energyMultiplier;
    public static Omniconfig.PerhapsParameter chargingDamageResistance;
    public static final List<ResourceLocation> IngredientList = new ArrayList<>();
    private static final String[] defaultIngredientList = new String[]{
            "enigmaticlegacy:cosmic_heart",
            "enigmaticlegacy:evil_ingot",
            "enigmaticlegacy:twisted_heart"
    };
    private static final Tier TIER = new ForgeTier(10, 3268, 10.8F, 8.0F, 64, BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.EMPTY);

    public AnnihilatingSword() {
        super(TIER, 11, -2.8F, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("DecisionofAnnihilation");
        onlyMainHandModifier = builder.comment("The damage modifier when offhand is empty. Measured as percentage.").min(100).getPerhaps("OnlyMainHandModifier", 100);
        damageEnchantmentPerModifier = builder.comment("The damage modifier per every level of damage enchantments. Measured as percentage.").min(10).getPerhaps("DamageEnchantmentPerModifier", 15);
        energyMultiplier = builder.comment("The damage multiplier per every level of energy. Measured as percentage.").min(10).getPerhaps("EnergyMultiplier", 20);
        chargingDamageResistance = builder.comment("The damage resistance when charging with the Decision of Annihilation. Measured as percentage.").max(90).min(0).getPerhaps("ChargingDamageResistance", 80);
        IngredientList.clear();
        builder.forceSynchronized(true);
        String[] list = builder.config.getStringList("DecisionofAnnihilationCraftingIngredients", "Balance Options", defaultIngredientList, "List of items that will be the crafting ingredients of the Decision of Annihilation. Examples: enigmaticlegacy:etherium_ingot. Changing this option required game restart to take effect.");
        Arrays.stream(list).forEach((entry) -> IngredientList.add(new ResourceLocation(entry)));
        builder.popPrefix();
    }

    public static void parry(Level level, ServerPlayer blocker, DamageSource source, float damage, ItemStack stack) {
        int tick = stack.getUseDuration() - blocker.getUseItemRemainingTicks();
        stack.hurtAndBreak(Mth.floor(damage / 2), blocker, user -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        blocker.getCooldowns().addCooldown(stack.getItem(), Mth.clamp(tick, 30, 50) - (tick <= 8 ? 5 : 0));
        blocker.swing(blocker.getUsedItemHand());
        blocker.stopUsingItem();
        blocker.getPersistentData().putInt("AnniInvTime", 15);
        if (source.getEntity() != null)
            EnigmaticAddons.packetInstance.send(PacketDistributor.PLAYER.with(() -> blocker), new PacketPlaySound(SoundEvents.SHIELD_BLOCK, 0.5F, 1.1F + 0.2F * blocker.getRandom().nextFloat()));
        if (tick <= 8 && source.getEntity() != null) {
            EnigmaticAddons.packetInstance.send(PacketDistributor.PLAYER.with(() -> blocker), new PacketPlaySound(SoundEvents.ANVIL_LAND, 0.5F, 1.2F + 0.3F * blocker.getRandom().nextFloat()));
            addEnergy(level, stack, blocker);
        }
        float dmg = (float) (blocker.getAttributes().getValue(Attributes.ATTACK_DAMAGE) + 8 + damage);
        if (source.getDirectEntity() instanceof LivingEntity attacker) {
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(1.2));
            for (LivingEntity entity : entities) {
                if (entity == blocker) continue;
                double xRatio = blocker.getX() - entity.getX();
                double zRatio = blocker.getZ() - entity.getZ();
                entity.hasImpulse = true;
                Vec3 vec = entity.getDeltaMovement();
                Vec3 add = new Vec3(xRatio, 0.0, zRatio).normalize().scale(tick < 25 ? 1.2F : 0.55F).scale(1.0 - 0.5 * entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                if (tick <= 8) entity.hurt(entity.damageSources().mobAttack(blocker), dmg * 0.5F);
                entity.setDeltaMovement(vec.x / 2.0 - add.x, entity.onGround() ? Math.min(0.4, vec.y / 2.0 + 0.5) : vec.y, vec.z / 2.0 - add.z);
            }
        } else if (source.getDirectEntity() instanceof Projectile projectile && tick <= 8) {
            boolean discard = !(projectile instanceof AbstractArrow arrow) || arrow.pickup != AbstractArrow.Pickup.ALLOWED;
            if (discard) {
                AbyssProjectile abyssProjectile = new AbyssProjectile(blocker.level(), blocker);
                abyssProjectile.setDamage(dmg * 4);
                if (source.getEntity() != null) abyssProjectile.setTarget(source.getEntity());
                abyssProjectile.setPos(projectile.position());
                abyssProjectile.setDeltaMovement(projectile.getDeltaMovement().scale(-1.6));
                blocker.level().addFreshEntity(abyssProjectile);
                projectile.discard();
            }
        }
    }

    private static void addEnergy(Level level, ItemStack stack, ServerPlayer player) {
        boolean abyssBoost = SuperAddonHandler.isAbyssBoost(player);
        int part = stack.getOrCreateTag().getInt("AnniEnergyPart");
        part += player.getRandom().nextInt(abyssBoost ? 6 : 4) + 1;
        if (part > 6) {
            part -= 6;
            int energy = stack.getOrCreateTag().getInt("AnniEnergy");
            EnigmaticAddons.packetInstance.send(PacketDistributor.PLAYER.with(() -> player), new PacketPlaySound(EnigmaticSounds.CHARGED_ON, 1.8F, 0.5F + 0.25F * energy));
            stack.getOrCreateTag().putInt("AnniEnergy", Math.min(abyssBoost ? 5 : 3, energy + 1));
        }
        stack.getOrCreateTag().putInt("AnniEnergyPart", part);
    }

    public static void sweep(Player player, int level, float damage) {
        damage *= (1.2F + 0.08F * level);
        Vec3 position = player.position().add(0, player.getBbHeight() / 2, 0);
        Vec3 offset = new Vec3(level + 3, 0.5, level + 3);
        AABB box = new AABB(position.subtract(offset), position.add(offset));
        float ratio = (float) (Math.PI / 180.0F) * player.getYRot();
        box = box.move(-level * Mth.sin(ratio), 0, level * Mth.cos(ratio));
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, box, Entity::isAlive);
        for (LivingEntity entity : entities) {
            if (entity == player) continue;
            if (player.isAlliedTo(entity)) continue;
            if (entity instanceof ArmorStand armorStand && armorStand.isMarker()) continue;
            if (entity.distanceTo(player) > player.getEntityReach() + level) continue;
            if (entity.distanceTo(player) < level || entity.position().subtract(player.position()).dot(player.getForward()) > 0) {
                entity.knockback(level * 0.5 + 0.4, Mth.sin(ratio), -Mth.cos(ratio));
                entity.hurt(player.damageSources().playerAttack(player), damage);
                if (entity.level() instanceof ServerLevel server) {
                    server.sendParticles(EnigmaticAddonParticles.ABYSS_CHAOS, entity.getX(), entity.getEyeY(), entity.getZ(), 16, entity.getBbWidth(), entity.getBbHeight() / 2, entity.getBbWidth(), 0);
                }
            }
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.2F, 0.2F);
        player.swing(InteractionHand.MAIN_HAND);
        player.resetAttackStrengthTicker();
        player.sweepAttack();
        player.getPersistentData().putFloat("AnniSweepDamage", damage);
        player.getPersistentData().putInt("AnniSweepTick", 5);
        player.getPersistentData().putInt("AnniSweepEnergy", level);
    }

    public static void rangeAttack(Player player, int level, float damage) {
        for (int i = 0; i < 4 * level; i++) {
            AbyssProjectile abyssProjectile = new AbyssProjectile(player.level(), player);
            abyssProjectile.setDamage(damage);
            abyssProjectile.setPos(new Vec3(player.getRandomX(1), player.getRandomY(), player.getRandomZ(1)));
            abyssProjectile.setDeltaMovement(abyssProjectile.position().subtract(player.position()).add(0.0, 0.32, 0.0).scale(1.5));
            player.level().addFreshEntity(abyssProjectile);
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.25F, 1.5F);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.2F, 0.2F);
        player.swing(InteractionHand.MAIN_HAND);
        player.resetAttackStrengthTicker();
        player.sweepAttack();
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        int energy = stack.getOrCreateTag().getInt("AnniEnergy");
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        int damage = 15 + stack.getOrCreateTag().getInt("AnniSoulCount") / 10;
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", damage + energy * 3, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", energy * 0.1 - 2.8, AttributeModifier.Operation.ADDITION));
        if (energy > 0) builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(UUID.fromString("2e24be65-2fe7-4311-8892-8f9eb7d51f80"), "Weapon modifier", energy * 0.3, AttributeModifier.Operation.ADDITION));
        return slot == EquipmentSlot.MAINHAND ? builder.build() : super.getAttributeModifiers(slot, stack);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        LocalPlayer player = Minecraft.getInstance().player;
        list.add(Component.translatable("tooltip.enigmaticaddons.annihilatingSwordOwner").withStyle(EnigmaticAddons.Acceptors.contains(player.getUUID()) ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE));
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.annihilatingSword1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.annihilatingSword2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.annihilatingSword3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.annihilatingSword4");
            if (player != null && player.getOffhandItem().equals(stack)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.annihilatingSword5Off");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.annihilatingSword6Off");
            } else {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.annihilatingSword5Main", ChatFormatting.GOLD, String.format("+%d%%", getMainHandModifier(stack)));
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.annihilatingSword6Main");
            }
            int energy = stack.getOrCreateTag().getInt("AnniEnergy");
            if (energy > 0) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.annihilatingSword7", ChatFormatting.GOLD, energy);
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.annihilatingSword8", ChatFormatting.GOLD, String.format("×%.02f", 1 + energy * energyMultiplier.getValue().asModifier()));
            }
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.indicateWorthyOnesOnly(list);
            if (player != null && SuperAddonHandler.isAbyssBoost(player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.abyssBoost");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            }
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.indicateCursedOnesOnly(list);
        }
        if (stack.isEnchanted()) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (!SuperpositionHandler.isTheWorthyOne(player)) return InteractionResultHolder.pass(itemInHand);
        if (hand.equals(InteractionHand.OFF_HAND) && player.getMainHandItem().isEmpty()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemInHand);
        } else if (hand.equals(InteractionHand.MAIN_HAND) && player.getOffhandItem().isEmpty()) {
            if (itemInHand.getOrCreateTag().getInt("AnniEnergy") > 0) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(itemInHand);
            }
        }
        return InteractionResultHolder.pass(itemInHand);
    }

    public void releaseUsing(ItemStack stack, Level world, LivingEntity living, int tick) {
        if (living instanceof Player player && player.getOffhandItem().is(this)) {
            player.getCooldowns().addCooldown(this, 8);
        }
    }

    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (entity instanceof Player player && player.getMainHandItem().is(this)) {
            ItemStack mainHandItem = player.getMainHandItem();
            int energy = mainHandItem.getOrCreateTag().getInt("AnniEnergy");
            if (energy > 0) {
                float damage = (float) (player.getAttributes().getValue(Attributes.ATTACK_DAMAGE)) + EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
//                if (energy >= 5)
//                    player.sendSystemMessage(Component.literal("AbyssFinalAttack! (But not)"));
                if (SuperAddonHandler.isAbyssBoost(player)) {
                    sweep(player, energy, damage);
                    rangeAttack(player, energy, damage);
                } else if (player.onGround() || player.level().getBlockState(player.blockPosition().below(2)).canOcclude()) {
                    sweep(player, energy, damage);
                } else rangeAttack(player, energy, damage);
                player.getCooldowns().addCooldown(this, 100 - energy * 8);
                stack.hurtAndBreak(energy * 3, player, user -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                int level = stack.getEnchantmentLevel(Enchantments.UNBREAKING);
                if (level > 0 && player.getRandom().nextInt(level + 3) < level) {
                    mainHandItem.getOrCreateTag().putInt("AnniEnergy", energy / 2);
                } else mainHandItem.getOrCreateTag().remove("AnniEnergy");
            }
        }
        return stack;
    }

    public void inventoryTick(ItemStack stack, Level world, Entity entity, int id, boolean selected) {
        if (!world.isClientSide()) {
            CompoundTag tag = stack.getOrCreateTag();
            if (entity instanceof Player player && tag.hasUUID("AnniOwner")) {
                UUID playerUUID = player.getUUID();
                long code = tag.getLong("AnniCode");
                if (code != SuperAddonHandler.encodeUUID(playerUUID)) {
                    if (EnigmaticAddons.Acceptors.contains(playerUUID)) {
                        tag.putLong("AnniCode", SuperAddonHandler.encodeUUID(playerUUID));
                    } else stack.shrink(1);
                }
                else if (!tag.getUUID("AnniOwner").equals(player.getUUID())) stack.shrink(1);
            } else stack.shrink(1);
        }
    }

    private int getMainHandModifier(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        int maxLevel = 0;
        for (Enchantment enc : enchantments.keySet()) {
            if (enc instanceof DamageEnchantment && enchantments.get(enc) > maxLevel) maxLevel = enchantments.get(enc);
        }
        return onlyMainHandModifier.getValue().asPercentage() + maxLevel * damageEnchantmentPerModifier.getValue().asPercentage();
    }

    private Multimap<Attribute, AttributeModifier> getAttackSpeedBonus() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("015012f8-8638-4c33-8f4a-b187680e8b80"), "Block Bonus", 2, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    public @NotNull AABB getSweepHitBox(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity target) {
        return target.getBoundingBox().inflate(1.75, 0.3, 1.75);
    }

    public int getUseDuration(ItemStack stack) {
        int energy = stack.getOrCreateTag().getInt("AnniEnergy");
        return energy > 0 ? 24 : 32000;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }

    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.getPersistentData().getInt("AnniInvTime") > 0) {
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (event.getSource().getEntity() instanceof LivingEntity entity && entity.getMainHandItem().is(this)) {
            if ( entity.getOffhandItem().isEmpty())
                event.setAmount(event.getAmount() * (1 + 0.01F * getMainHandModifier(entity.getMainHandItem())));
            ItemStack mainHandItem = entity.getMainHandItem();
            int energy = mainHandItem.getOrCreateTag().getInt("AnniEnergy");
            if (energy > 0) event.setAmount(event.getAmount() * (1 + energy * energyMultiplier.getValue().asModifier()));
            if (EnigmaticAddons.Acceptors.contains(entity.getUUID())) {
                CuriosApi.getCuriosInventory(entity).ifPresent((handler) -> {
                    IItemHandlerModifiable curios = handler.getEquippedCurios();
                    for (int i = 0; i < handler.getSlots() - 1; ++i) {
                        if (curios.getStackInSlot(i).getItem() instanceof IEldritch)
                            event.setAmount(event.getAmount() * 1.05F);
                    }
                });
            }
        }
        if (victim.getUseItem().is(this) && victim.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            event.setAmount(event.getAmount() * (1 - chargingDamageResistance.getValue().asModifier()));
        }
        if (EnigmaticAddons.Acceptors.contains(victim.getUUID())) {
            CuriosApi.getCuriosInventory(victim).ifPresent((handler) -> {
                IItemHandlerModifiable curios = handler.getEquippedCurios();
                for (int i = 0; i < handler.getSlots() - 1; ++i) {
                    if (curios.getStackInSlot(i).getItem() instanceof IEldritch)
                        event.setAmount(event.getAmount() * 0.95F);
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAttackLast(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (event.getSource().getEntity() instanceof LivingEntity entity && entity.getMainHandItem().is(this)) {
            if (!entity.getOffhandItem().isEmpty()) return;
            CompoundTag data = victim.getPersistentData();
            float annihilation = data.getFloat("AnnihilationPoint") + 0.1F * event.getAmount() / victim.getHealth();
            if (entity.getRandom().nextFloat() < annihilation) {
                data.putBoolean("AnnihilationKill", true);
                if (victim.isAlive()) event.setAmount(event.getAmount() * 10F);
                if (entity.level() instanceof ServerLevel server) {
                    server.sendParticles(EnigmaticAddonParticles.ABYSS_CHAOS, victim.getX(), victim.getEyeY(), victim.getZ(), 16, victim.getBbWidth(), victim.getBbHeight() / 2, entity.getBbWidth(), 0);
                }
            }
            data.putFloat("AnnihilationPoint", annihilation);
        }
    }

    @SubscribeEvent
    public void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            if (entity.getPersistentData().getBoolean("AnnihilationKill")) entity.hurt(entity.damageSources().genericKill(), entity.getMaxHealth() / 3.0F);
            int anniInvTime = entity.getPersistentData().getInt("AnniInvTime");
            if (anniInvTime > 0) {
                entity.getAttributes().addTransientAttributeModifiers(getAttackSpeedBonus());
                entity.getPersistentData().putInt("AnniInvTime", anniInvTime - 1);
            } else {
                entity.getAttributes().removeAttributeModifiers(getAttackSpeedBonus());
                entity.getPersistentData().remove("AnniInvTime");
            }
        } else {
            float powerTime = entity.getPersistentData().getFloat("AnniPowerTick");
            entity.getPersistentData().putFloat("AnniLastTick", powerTime);
            if (entity.getUseItem().is(this) && entity.getUsedItemHand().equals(InteractionHand.MAIN_HAND))
                powerTime = Math.min(24F, entity.getUseItemRemainingTicks());
            else if (24F - powerTime < 1F) powerTime = 24F;
            else powerTime += (24F - powerTime) * 0.2F;
            entity.getPersistentData().putFloat("AnniPowerTick", powerTime);
        }

        if (entity instanceof Player player && SuperpositionHandler.isTheWorthyOne(player) && player.getPersistentData().getInt("AnniSweepTick") > 0) {
            if (player.level() instanceof ServerLevel server) {
                int energy = player.getPersistentData().getInt("AnniSweepEnergy");
                int tick = player.getPersistentData().getInt("AnniSweepTick");
                float base = (float) (Math.PI / 180.0F) * player.getYRot();
                double x = -(3 + 0.3 * energy) * Mth.sin((float) (base + (tick - 3) * 25F * (Math.PI / 180.0F))) + player.getX() - Mth.sin(base) * 0.6F;
                double y = player.getY(0.5);
                double z = (3 + 0.3 * energy) * Mth.cos((float) (base + (tick - 3) * 25F * (Math.PI / 180.0F))) + player.getZ() + Mth.cos(base) * 0.6F;
                server.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.25F, 1.5F);
                server.sendParticles(EnigmaticAddonParticles.ABYSS_CHAOS, x, y, z, 32, 1, 1, 1, 0.02);
                server.sendParticles(ParticleTypes.WITCH, x, y, z, 32, 1, 1, 1, 0.02);
                server.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 0, 0.0, 0.0, 0.0, 0.0);
                player.getPersistentData().putInt("AnniSweepTick", tick - 1);
                float damage = player.getPersistentData().getFloat("AnniSweepDamage");
                double o = 0.8 + 0.45 * energy;
                AABB aabb = new AABB(x - o, y - o, z - o, x + o, y + o, z + o);
                List<LivingEntity> entities = server.getEntitiesOfClass(LivingEntity.class, aabb, Entity::isAlive);
                for (LivingEntity target : entities) {
                    if (target == player) continue;
                    if (player.isAlliedTo(target)) continue;
                    if (target instanceof ArmorStand && ((ArmorStand) target).isMarker()) continue;
                    target.hurt(SuperAddonHandler.damageSource(target, EnigmaticAddonDamageTypes.ABYSS, player), damage * 0.4F);
                }
            }
        }
    }

    public static class Unknown extends SwordItem implements IEldritch {
        private static final List<EntityType<?>> list = new ArrayList<>();
        public Unknown() {
            super(TIER, 11, -2.2F, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
            MinecraftForge.EVENT_BUS.register(this);
        }

        private int getCount(ItemStack stack, Level world, @Nullable Player player) {
            CompoundTag tag = stack.getOrCreateTag();
            if (player != null && EnigmaticAddons.Acceptors.contains(player.getUUID())) {
                tag.putInt("AnniCacheDemand", 20);
                return 20;
            }
            if (list.isEmpty()) {
                list.addAll(ForgeRegistries.ENTITY_TYPES.getValues());
                tag.remove("AnniCacheIndex");
            }
            if (player != null && player.tickCount % 10 == 0) {
                int index = tag.getInt("AnniCacheIndex");
                if (index < list.size()) {
                    try {
                        EntityType<?> entityType = list.get(index);
                        if (entityType.create(world) instanceof LivingEntity)
                            tag.putInt("AnniCacheIndex", index + 1);
                        else list.remove(index);
                    } catch (Exception exception) {
                        list.remove(index);
                    }
                } else tag.remove("AnniCacheIndex");
                int i = 20 + Mth.floor(Math.pow(list.size(), 0.875F) * 0.8974862513F);
                tag.putInt("AnniCacheDemand", i);
                return i;
            }
            return tag.getInt("AnniCacheDemand");
        }

        public void inventoryTick(ItemStack stack, Level world, Entity entity, int id, boolean selected) {
            CompoundTag tag = stack.getOrCreateTag();
            if (entity instanceof Player player && SuperpositionHandler.isTheWorthyOne(player) && player.getItemBySlot(EquipmentSlot.HEAD).equals(stack) && tag.getBoolean("AnniAvailable")) {
                ListTag list = stack.getOrCreateTag().getList("AnniSoulCount", 10);
                if (list.size() > getCount(stack, world, player)) {
                    world.playSound(player, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    ItemStack sword = EnigmaticAddonItems.ANNIHILATING_SWORD.getDefaultInstance();
                    UUID uuid = player.getUUID();
                    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    sword.getOrCreateTag().putUUID("AnniOwner", uuid);
                    sword.getOrCreateTag().putLong("AnniCode", SuperAddonHandler.encodeUUID(uuid));
                    sword.getOrCreateTag().putInt("AnniSoulCount", EnigmaticAddons.Acceptors.contains(player.getUUID()) ? list.size() * 5 :list.size());
                    PermanentItemEntity itemEntity = new PermanentItemEntity(world, player.getX(), player.getY(0.6), player.getZ(), sword);
                    itemEntity.setOwnerId(uuid);
                    itemEntity.setThrowerId(uuid);
                    world.addFreshEntity(itemEntity);
                }
            }
        }

        public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
            ListTag list = stack.getOrCreateTag().getList("AnniSoulCount", 10);
            int count = getCount(stack, EnigmaticLegacy.PROXY.getCentralWorld(), null);
            if (count < 10) return super.getAttributeModifiers(slot, stack);
            int multi = Mth.clamp(list.size() / (count / 10), 0, 9);
            if (multi <= 0) return super.getAttributeModifiers(slot, stack);
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.fromString("00a4d7d3-d8ef-4df9-b6a7-5ed39f6a7c24"), "?? modifier", -count * 0.1F, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.MAX_HEALTH, new AttributeModifier(UUID.fromString("f286bd6f-49ed-4f61-a14b-ba538a3c0f3f"), "?? modifier", -0.1 * multi, AttributeModifier.Operation.MULTIPLY_TOTAL));
            return slot == EquipmentSlot.HEAD ? builder.build() : super.getAttributeModifiers(slot, stack);
        }

        public EquipmentSlot getEquipmentSlot(ItemStack stack) {
            return stack.getOrCreateTag().getBoolean("AnniAvailable") ? EquipmentSlot.HEAD : null;
        }

        public boolean isFoil(ItemStack stack) {
            return true;
        }

        public boolean isEnchantable(ItemStack stack) {
            return false;
        }

        @OnlyIn(Dist.CLIENT)
        public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.indicateWorthyOnesOnly(list);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onDeath(LivingDeathEvent event) {
            LivingEntity victim = event.getEntity();
            if (event.getSource().getEntity() instanceof Player player && SuperpositionHandler.isTheWorthyOne(player)) {
                ItemStack item = player.getItemBySlot(EquipmentSlot.HEAD);
                if (item.is(this)) {
                    ListTag tag = item.getOrCreateTag().getList("AnniSoulCount", 10);
                    CompoundTag compoundtag = new CompoundTag();
                    compoundtag.putString("id", victim.getType().getDescriptionId());
                    if (!tag.contains(compoundtag)) tag.add(compoundtag);
                    item.addTagElement("AnniSoulCount", tag);
                }
            }
        }
    }
}

package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.packets.clients.PacketExtradimensionParticles;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ExtradimensionalScepter extends ItemBase {
    public static Omniconfig.IntParameter transportingCooldown;
    public static Omniconfig.IntParameter overheatingCooldown;
    public static Omniconfig.IntParameter maxCombatCount;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public ExtradimensionalScepter() {
        super(ItemBase.getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).durability(240).fireResistant());
        MinecraftForge.EVENT_BUS.register(this);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 1.5, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -1.6F, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("ScepterofExtradimensional");
        transportingCooldown = builder.comment("The cooldown of Transporting Mode. Measured in ticks.").min(200).getInt("TransportingCooldown", 280);
        overheatingCooldown = builder.comment("The cooldown of Combat Mode when using continuously to long. Measured in ticks.").min(100).getInt("OverheatCooldown", 150);
        maxCombatCount = builder.comment("The max count to transporting enemies of Combat Mode.").getInt("MaxCombatCount", 5);
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (!Helper.isCombatMode(stack)) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.extradimensionalScepter1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.extradimensionalScepterCooldown", ChatFormatting.GOLD, transportingCooldown.getValue() / 20);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.extradimensionalScepterModeS");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.extradimensionalScepter2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.extradimensionalScepter3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.extradimensionalScepterCooldown", ChatFormatting.GOLD, overheatingCooldown.getValue() / 20);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.extradimensionalScepterModeC");
        }
        if (Helper.isValid(stack) && !Helper.isCombatMode(stack)) {
            CompoundTag storeEntityTag = Helper.getInfo(stack);
            if (storeEntityTag != null) {
                String customName = storeEntityTag.getString("CustomName");
                MutableComponent type = Component.translatable(Helper.getType(stack).getDescriptionId());
                if (customName.isEmpty())
                    ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.extradimensionalScepterInfo", ChatFormatting.GOLD, type);
                else {
                    Component custom = Component.Serializer.fromJson(customName);
                    ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.extradimensionalScepterInfo_alt", ChatFormatting.GOLD, custom, type);
                }
            }
        }
        if (stack.isEnchanted()) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity user) {
        stack.hurtAndBreak(2, user, consumer -> consumer.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (Helper.validScepter(player, itemInHand)) {
            if (player.isCrouching()) {
                Helper.switchMode(itemInHand, player);
                return InteractionResultHolder.success(itemInHand);
            }
            if (Helper.isCombatMode(itemInHand)) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(itemInHand);
            }
        }
        return super.use(level, player, hand);
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (user instanceof Player player) {
            int pass = this.getUseDuration(stack);
            stack.hurtAndBreak(this.getUseDuration(stack) / 8, player, consumer -> consumer.broadcastBreakEvent(player.getUsedItemHand()));
            player.getCooldowns().addCooldown(this, Helper.getOverheatCooldown(player));
            player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS);
            player.swing(player.getUsedItemHand());
        }
        return stack;
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int tick) {
        if (entity instanceof Player player) {
            int pass = this.getUseDuration(stack) - tick;
            stack.hurtAndBreak(pass / 8, player, consumer -> consumer.broadcastBreakEvent(player.getUsedItemHand()));
            player.getCooldowns().addCooldown(this, Helper.getOverheatCooldown(player) / 5 + pass / 5);
            player.swing(player.getUsedItemHand());
        }
    }

    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int tick) {
        Vec3 view = user.getViewVector(0.0F).scale(0.4);
        List<LivingEntity> entities = new ArrayList<>();
        double x = user.getX(), z = user.getZ(), y = user.getEyeY() - 0.25F;
        AABB box = new AABB(x - 0.2, y - 0.2, z - 0.2, x + 0.2, y + 0.2, z + 0.2);
        float rand = user.getRandom().nextFloat();
        x += view.x * rand;
        z += view.z * rand;
        y += view.y * rand;
        for (int i = 1; i < 56; i++) {
            entities = level.getEntitiesOfClass(LivingEntity.class, box,
                    living -> living != user && user.canAttack(living) && Helper.validTarget(user, living, 5.0F));
            if (!entities.isEmpty()) break;
            box = box.move(view);
            x = ((view.x + x) * 2 + (user.getRandom().nextFloat() - 0.5F) * 0.5F) / 2;
            z = ((view.z + z) * 2 + (user.getRandom().nextFloat() - 0.5F) * 0.5F) / 2;
            y = ((view.y + y) * 2 + (user.getRandom().nextFloat() - 0.5F) * 0.5F) / 2;
            if (user.tickCount % 4 == 0) {
                if (level.isClientSide()) level.addParticle(ParticleTypes.WITCH, x, y, z, 0, -0.5, 0);
                else
                    level.playSound(null, user.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.4F, 1.2F + 0.2F * user.getRandom().nextFloat());
            }
            if (level.getBlockState(BlockPos.containing(x, y, z)).canOcclude()) break;
        }
        if (!entities.isEmpty()) {
            if (!level.isClientSide()) {
                for (LivingEntity entity : entities) {
                    int counter = entity.getPersistentData().getInt("ExtradimensionCounter");
                    int add = 2 * Mth.floor(Math.sqrt(Math.max((user.getHealth() + user.getAttribute(Attributes.ATTACK_DAMAGE).getValue()) * 5, 0)));
                    entity.getPersistentData().putInt("ExtradimensionCounter", counter + add);
                    int healthCounter = Math.max(Mth.floor(entity.getHealth() * 20), 200);
                    if (counter > healthCounter / 2)
                        entity.hurt(SuperAddonHandler.damageSource(entity, DamageTypes.FELL_OUT_OF_WORLD, user), (float) (user.getAttributes().getValue(Attributes.ATTACK_DAMAGE) / 4.0F));
                }
            }
        }
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (Helper.validTarget(player, entity, 2.5) && Helper.validScepter(player, itemInHand)) {
            if (Helper.isCombatMode(stack) || player.isCrouching()) return InteractionResult.PASS;
            Level level = player.level();
            EntityType<?> type = Helper.getType(itemInHand);
            if (type == null) return InteractionResult.FAIL;
            if (!level.isClientSide()) {
                if (Helper.isValid(itemInHand)) {
                    return InteractionResult.FAIL;
                    /* Invalid function
                    Entity storedEntity = type.create(level);
                    storedEntity.load(Helper.getInfo(itemInHand));
                    double newY = entity.getY() + entity.getPassengersRidingOffset() + storedEntity.getMyRidingOffset();
                    storedEntity.setPos(entity.getX(), newY, entity.getZ());
                    storedEntity.setDeltaMovement(Vec3.ZERO);
                    if (!storedEntity.startRiding(entity, false)) return InteractionResult.PASS;
                    level.addFreshEntity(storedEntity);
                    Helper.setValid(itemInHand, false);
                    */
                } else {
                    CompoundTag storedEntityInfo = Helper.getInfo(itemInHand);
                    Helper.setAll(itemInHand, storedEntityInfo, entity);
                    entity.discard();
                }
                level.playSound(null, player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS);
                itemInHand.hurtAndBreak(2, player, consumer -> consumer.broadcastBreakEvent(player.getUsedItemHand()));
                player.getCooldowns().addCooldown(this, Helper.getTransCooldown(player));
                EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 16.0, level.dimension())),
                        new PacketExtradimensionParticles(entity.getX(), entity.getY(), entity.getZ(), entity.getBbWidth(), entity.getBbHeight(), 1));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemInHand = context.getItemInHand();
        if (!Helper.isCombatMode(itemInHand) && context.getClickedFace().equals(Direction.UP) && Helper.validScepter(player, itemInHand)) {
            if (!Helper.isValid(itemInHand) || player.isCrouching()) return InteractionResult.PASS;
            EntityType<?> type = Helper.getType(itemInHand);
            if (type == null) return InteractionResult.FAIL;
            if (!level.isClientSide()) {
                player.getCooldowns().addCooldown(this, Helper.getTransCooldown(player));
                itemInHand.hurtAndBreak(2, player, consumer -> consumer.broadcastBreakEvent(player.getUsedItemHand()));
                Entity entity = type.create(level);
                entity.load(Helper.getInfo(itemInHand));
                entity.setPos(context.getClickLocation());
                entity.setDeltaMovement(Vec3.ZERO);
                level.addFreshEntity(entity);
                Helper.setValid(itemInHand, false);
                player.level().playSound(null, player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS);
                EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 16.0, level.dimension())),
                        new PacketExtradimensionParticles(entity.getX(), entity.getY(), entity.getZ(), entity.getBbWidth(), entity.getBbHeight(), 1));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public int getUseDuration(ItemStack stack) {
        return 100;
    }

    public boolean isValidRepairItem(ItemStack self, ItemStack stack) {
        return stack.is(EnigmaticItems.EXTRADIMENSIONAL_EYE) || super.isValidRepairItem(self, stack);
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 24;
    }

    @SubscribeEvent
    public void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (!entity.isAlive() || level.isClientSide()) return;
        int threshold = Math.max(Mth.floor(entity.getHealth() * 20), 200);
        int counter = entity.getPersistentData().getInt("ExtradimensionCounter");
        if (counter > 0) {
            if (counter > threshold) {
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(10), living -> entity.canAttack(living) && Helper.validTarget(entity, living, 1.4) && !(living instanceof Player));
                entities.sort((living1, living2) -> Float.compare(living2.getHealth(), living1.getHealth()));
                List<LivingEntity> subList = entities.subList(0, Math.min(maxCombatCount.getValue(), entities.size()));
                if (!subList.contains(entity)) subList.add(entity);
                level.playSound(null, entity.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.5F, 0.8F + 0.4F * entity.getRandom().nextFloat());
                for (LivingEntity target : subList) {
                    PermanentItemEntity extra = new PermanentItemEntity(level, target.getX(), target.getY(0.5), target.getZ());
                    ItemStack stack = EnigmaticItems.EXTRADIMENSIONAL_EYE.getDefaultInstance();
                    CompoundTag storedEntityInfo = Helper.getInfo(stack);
                    target.getPersistentData().remove("ExtradimensionCounter");
                    Helper.setAll(stack, storedEntityInfo, target);
                    float damage = 0.0F;
                    LivingEntity lastAttacker = target.getLastAttacker();
                    if (lastAttacker != null && lastAttacker.getMainHandItem().is(this))
                        damage = (float) lastAttacker.getAttribute(Attributes.ATTACK_DAMAGE).getValue() / 2.0F;
                    float newHealth = Math.min(target.getHealth() * 0.9F, target.getHealth() - damage);
                    storedEntityInfo.putFloat("Health", Math.max(newHealth, 0.5F));
                    stack.getOrCreateTag().put("ExtradimensionalEntity", storedEntityInfo);
                    extra.setItem(stack);
                    extra.setCustomName(Component.literal("ExtradimensionalLockSpace"));
                    extra.getPersistentData().putInt("ExtradimensionalLockTimer", Mth.floor(Math.sqrt(threshold - 200) * 4) + 120);
                    extra.setCustomNameVisible(false);
                    extra.setInfinitePickupDelay();
                    extra.setInvulnerable(true);
                    level.addFreshEntity(extra);
                    target.discard();
                    EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(target.getX(), target.getY(), target.getZ(), 16.0, level.dimension())),
                            new PacketExtradimensionParticles(target.getX(), target.getY(), target.getZ(), target.getBbWidth(), target.getBbHeight(), 1));
                }
                return;
            }
            counter = Math.max(0, counter - Math.max(1, Mth.ceil(Math.pow(threshold, 0.3))));
            if (counter == 0) entity.getPersistentData().remove("ExtradimensionCounter");
            else entity.getPersistentData().putInt("ExtradimensionCounter", counter);
        }
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getAttributeModifiers(slot, stack);
    }

    public static class Helper {
        public static int getTransCooldown(Player player) {
            return player.getAbilities().instabuild ? 5 : transportingCooldown.getValue();
        }

        public static int getOverheatCooldown(Player player) {
            return player.getAbilities().instabuild ? 20 : overheatingCooldown.getValue();
        }

        public static boolean validScepter(Player player, ItemStack stack) {
            return !player.getCooldowns().isOnCooldown(EnigmaticAddonItems.EXTRADIMENSIONAL_SCEPTER) && stack.getDamageValue() < stack.getMaxDamage();
        }

        public static boolean validTarget(LivingEntity from, LivingEntity to, double threshold) {
            boolean creative = from instanceof Player player && player.getAbilities().instabuild;
            return !(to instanceof Player) && (creative || from.getHealth() * threshold > to.getHealth());
        }

        public static boolean isCombatMode(ItemStack stack) {
            return stack.hasTag() && stack.getTag().getBoolean("CombatMode");
        }

        public static void switchMode(ItemStack stack, Player player) {
            stack.getTag().putBoolean("CombatMode", !isCombatMode(stack));
            player.level().playSound(null, player.blockPosition(), isCombatMode(stack) ? EnigmaticSounds.CHARGED_ON : EnigmaticSounds.CHARGED_OFF, SoundSource.PLAYERS);
        }

        public static @Nullable EntityType<?> getType(ItemStack stack) {
            if (stack.hasTag()) {
                String type = stack.getTag().getString("ExtradimensionalType");
                EntityType<?> value = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(type));
                if (value == null) setValid(stack, false);
                return value;
            }
            return null;
        }

        public static CompoundTag getInfo(ItemStack stack) {
            return !stack.hasTag() ? new CompoundTag() : stack.getTag().getCompound("ExtradimensionalEntity");
        }

        public static boolean isValid(ItemStack stack) {
            return stack.hasTag() && stack.getTag().getBoolean("ExtradimensionalValid");
        }

        public static void setValid(ItemStack stack, boolean valid) {
            stack.getOrCreateTag().putBoolean("ExtradimensionalValid", valid);
            if (!valid) stack.getTag().remove("ExtradimensionalEntity");
        }

        public static void setAll(ItemStack stack, CompoundTag tag, LivingEntity entity) {
            entity.saveWithoutId(tag);
            tag.remove("UUID");
            stack.getOrCreateTag().put("ExtradimensionalEntity", tag);
            ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            stack.getOrCreateTag().putString("ExtradimensionalType", key.toString());
            setValid(stack, true);
        }
    }
}
package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.packets.clients.PacketDescendingChaos;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.IBindable;
import com.aizistral.enigmaticlegacy.api.items.IEldritch;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.objects.TransientPlayerData;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.aizistral.enigmaticlegacy.packets.server.PacketUpdateElytraBoosting;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.caelus.api.CaelusApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ChaosElytra extends ItemBaseCurio implements IBindable, IEldritch {
    private static final AttributeModifier ELYTRA_MODIFIER = new AttributeModifier(UUID.fromString("446f9584-38bb-4fc0-bbed-16d126e377a7"), "enigmaticaddons:elytra_modifier", 1.0, AttributeModifier.Operation.ADDITION);
    public static Omniconfig.DoubleParameter flyingSpeedModifier;
    public static Omniconfig.DoubleParameter descendingPowerModifier;
    public static Omniconfig.IntParameter descendingCooldown;
    public static Omniconfig.PerhapsParameter damageResistance;
    @OnlyIn(Dist.CLIENT)
    private static boolean isBoosting;
    private Vec3 lastMovement = Vec3.ZERO;
    private int flyingTick = 0;

    public ChaosElytra() {
        super(ItemBaseCurio.getDefaultProperties().durability(3600).fireResistant().rarity(Rarity.EPIC));
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("TheArroganceofChaos");
        flyingSpeedModifier = builder.comment("The flying speed modifier when elytra boost.").max(10).min(1).getDouble("FlyingSpeedModifier", 1.6);
        descendingPowerModifier = builder.comment("The damage modifier when hit the ground with elytra boost.").max(10).min(1).getDouble("DescendingPowerModifier", 1.6);
        descendingCooldown = builder.comment("The cooldown of special descending skill of The Arrogance of Chaos.").max(2400).min(200).getInt("DescendingCooldown", 500);
        damageResistance = builder.comment("The special damage resistance of The Arrogance of Chaos. Measured in percentage.").max(100).min(0).getPerhaps("DamageResistance", 80);
    }

    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticElytra1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticElytra2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticElytra3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.chaosElytra1", ChatFormatting.GOLD, damageResistance + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.chaosElytra2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.chaosElytra3");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateWorthyOnesOnly(list);
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (!(context.entity() instanceof Player player)) {
            return false;
        } else {
            return SuperpositionHandler.isTheWorthyOne(player) && super.canEquip(context, stack);
        }
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int id, boolean selected) {
        if (entity instanceof Player player && !SuperpositionHandler.isTheWorthyOne(player)) {
            ItemStack itemStack = player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack copy = itemStack.copy();
            if (itemStack.is(this)) {
                Block.popResource(level, player.blockPosition(), itemStack);
                player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            }
        }
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            if (player.level().isClientSide) this.handleBoosting(player);
        }
        LivingEntity livingEntity = context.entity();
        int ticks = livingEntity.getFallFlyingTicks();
        if (ticks > 0 && livingEntity.isFallFlying()) {
            stack.elytraFlightTick(livingEntity, ticks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleBoosting(Player player) {
        if (Minecraft.getInstance().player == player) {
            PacketDistributor.PacketTarget target;
            if (Minecraft.getInstance().options.keyJump.isDown() && this.boostPlayer(player)) {
                if (!isBoosting) {
                    target = PacketDistributor.SERVER.noArg();
                    isBoosting = true;
                    EnigmaticLegacy.packetInstance.send(target, new PacketUpdateElytraBoosting(true));
                }
            } else if (isBoosting) {
                target = PacketDistributor.SERVER.noArg();
                isBoosting = false;
                EnigmaticLegacy.packetInstance.send(target, new PacketUpdateElytraBoosting(false));
            }
        }
    }

    private boolean boostPlayer(Player player) {
        if (player.isFallFlying()) {
            Vec3 look = player.getLookAngle().scale(flyingSpeedModifier.getValue());
            Vec3 move = player.getDeltaMovement();
            player.setDeltaMovement(move.add(look.x * 0.1 + (look.x * 1.5 - move.x) * 0.5, look.y * 0.1 + (look.y * 1.5 - move.y) * 0.5, look.z * 0.1 + (look.z * 1.5 - move.z) * 0.5));
            return true;
        } else {
            return false;
        }
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        return HashMultimap.create();
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        EquipmentSlot slotForItem = Mob.getEquipmentSlotForItem(itemInHand);
        ItemStack itemBySlot = player.getItemBySlot(slotForItem);
        if (itemBySlot.isEmpty()) {
            player.setItemSlot(slotForItem, itemInHand.copy());
            if (!level.isClientSide()) player.awardStat(Stats.ITEM_USED.get(this));
            itemInHand.setCount(0);
            return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide());
        } else {
            return InteractionResultHolder.fail(itemInHand);
        }
    }

    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_ELYTRA, 1.0F, 1.0F);
    }

    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return entity instanceof Player player && SuperpositionHandler.isTheWorthyOne(player) && ElytraItem.isFlyEnabled(stack);
    }

    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (entity instanceof Player player) {
            if (!entity.level().isClientSide) {
                int nextFlightTick = flightTicks + 1;
                if (nextFlightTick % 10 == 0) {
                    if (nextFlightTick % 20 == 0) {
                        stack.hurtAndBreak(1, entity, (living) -> living.broadcastBreakEvent(EquipmentSlot.CHEST));
                    }
                    entity.gameEvent(GameEvent.ELYTRA_GLIDE);
                }
            } else this.handleBoosting(player);
            return true;
        } else return false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ItemStack stack = null;
            AttributeInstance attribute = event.player.getAttribute(CaelusApi.getInstance().getFlightAttribute());
            attribute.removeModifier(ELYTRA_MODIFIER);
            if (!attribute.hasModifier(ELYTRA_MODIFIER)) {
                stack = SuperAddonHandler.getChaosElytra(event.player);
                if (stack != null && stack.is(this) && ElytraItem.isFlyEnabled(stack)) {
                    attribute.addTransientModifier(ELYTRA_MODIFIER);
                }
            }

            if (!event.player.onGround() && event.player.isFallFlying()) this.flyingTick++;
            else this.flyingTick = 0;
            if (event.player instanceof ServerPlayer serverPlayer && SuperAddonHandler.getChaosElytra(event.player) != null) {
                if (serverPlayer.tickCount % 3 == 0) {
                    if (serverPlayer.isFallFlying()) lastMovement = serverPlayer.getDeltaMovement();
                    else lastMovement = Vec3.ZERO;
                }
                if (TransientPlayerData.get(serverPlayer).isElytraBoosting()) {
                    this.boostPlayer(serverPlayer);
                    if (stack != null && stack.is(this)) {
                        int flightTicks = serverPlayer.getFallFlyingTicks();
                        int nextFlightTick = flightTicks + 1;
                        if (nextFlightTick % 6 == 0) {
                            stack.hurtAndBreak(1, serverPlayer, (player) -> player.broadcastBreakEvent(EquipmentSlot.CHEST));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onFall(PlayerFlyableFallEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Player player) || !SuperpositionHandler.isTheWorthyOne(player)) return;
        if (SuperpositionHandler.getFullEquipment(player).stream().noneMatch((itemStack) -> itemStack.is(EnigmaticAddonItems.CHAOS_ELYTRA)))
            return;
        if (TransientPlayerData.get(player).isElytraBoosting()) {
            chaosDescending(player);
        }
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Player player) || !SuperpositionHandler.isTheWorthyOne(player)) return;
        if (SuperpositionHandler.getFullEquipment(player).stream().noneMatch((itemStack) -> itemStack.is(EnigmaticAddonItems.CHAOS_ELYTRA)))
            return;
        if (TransientPlayerData.get(player).isElytraBoosting()) {
            event.setCanceled(true);
            chaosDescending(player);
        }
    }

    private void chaosDescending(Player player) {
        if (player.getViewVector(0.0F).y < -0.95 && !player.getCooldowns().isOnCooldown(this) && this.spaceCheck(player.blockPosition(), player.level()) && this.flyingTick > 36) {
            if (!player.getAbilities().instabuild)
                player.getCooldowns().addCooldown(this, descendingCooldown.getValue());
            if (!player.level().isClientSide()) {
                EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(player.getX(), player.getY(), player.getZ(), 64.0, player.level().dimension())),
                        new PacketDescendingChaos(player.getX(), player.getY(), player.getZ()));
            }
            List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3.5 + lastMovement.length()));
            for (LivingEntity entity : entities) {
                if (entity == player) continue;
                Vec3 delta = entity.position().subtract(player.position()).normalize().scale(0.5);
                float modifier = Math.min(1.0F, 1.2F / entity.distanceTo(player));
                Vec3 vec = new Vec3(delta.x, 0, delta.z).normalize().scale(modifier);
                entity.addDeltaMovement(new Vec3(vec.x, entity.onGround() ? 1.2F * modifier : 0.0F, vec.z));
                entity.hurt(SuperAddonHandler.damageSource(entity, EnigmaticAddonDamageTypes.ABYSS, player), (float) (player.getAttribute(Attributes.ATTACK_DAMAGE).getValue() * Math.pow(descendingPowerModifier.getValue(), Math.abs(lastMovement.y))));
            }
        }
    }

    private boolean spaceCheck(BlockPos pos, Level level) {
        Iterable<BlockPos> iterable = BlockPos.betweenClosed(pos.offset(2, 2, 2), pos.offset(-2, -2, -2));
        int space = 0;
        for (BlockPos blockPos : iterable) {
            if (level.getBlockState(blockPos).isAir()) space += 3;
            else space -= 1;
        }
        return space > 0;
    }

    @SubscribeEvent
    public void onHurt(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player) || !SuperpositionHandler.isTheWorthyOne(player)) return;
        if (SuperpositionHandler.getFullEquipment(player).stream().noneMatch((itemStack) -> itemStack.is(EnigmaticAddonItems.CHAOS_ELYTRA)))
            return;
        DamageSource source = event.getSource();
        if (!(source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL))) {
            Entity directEntity = event.getSource().getDirectEntity();
            if (directEntity != null && directEntity.position().subtract(player.position()).dot(player.getForward()) < 0) {
                event.setAmount(event.getAmount() * damageResistance.getValue().asModifierInverted());
            }
        } else {
            event.setAmount(event.getAmount() * damageResistance.getValue().asModifierInverted());
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onPlayerTickClient(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.player.level().isClientSide()) {
            Player player = event.player;
            if (!SuperpositionHandler.isTheWorthyOne(player)) return;
            if (SuperpositionHandler.getFullEquipment(player).stream().noneMatch((itemStack) -> itemStack.is(EnigmaticAddonItems.CHAOS_ELYTRA)))
                return;
            if (TransientPlayerData.get(player).isElytraBoosting() && player.isFallFlying()) {
                int amount = 3;
                double rangeModifier = 0.1;
                for (int counter = 0; counter <= amount; ++counter) {
                    Vector3 vec = Vector3.fromEntityCenter(player);
                    vec = vec.add(Math.random() - 0.5, -1.0 + Math.random() - 0.5, Math.random() - 0.5);
                    player.level().addParticle(EnigmaticAddonParticles.ABYSS_CHAOS, true, vec.x, vec.y, vec.z, (Math.random() - 0.5) * 2.0 * rangeModifier, (Math.random() - 0.5) * 2.0 * rangeModifier, (Math.random() - 0.5) * 2.0 * rangeModifier);
                }
            }
        }
    }
}

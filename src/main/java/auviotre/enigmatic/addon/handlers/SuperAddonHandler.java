package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.api.events.LivingCurseBoostEvent;
import auviotre.enigmatic.addon.contents.enchantments.RedemptionCurseEnchantment;
import auviotre.enigmatic.addon.contents.items.BlessRing;
import auviotre.enigmatic.addon.contents.objects.bookbag.AntiqueBagCapability;
import auviotre.enigmatic.addon.contents.objects.bookbag.IAntiqueBagHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.*;

public class SuperAddonHandler {
    public static final UUID SCROLL_SLOT_UUID = UUID.fromString("36e2f84d-a453-467d-8836-41e2f42db78a");

    public static boolean isCurseBoosted(LivingEntity entity) {
        if (!OmniconfigAddonHandler.EnableCurseBoost.getValue()) return false;
        return entity.getPersistentData().getBoolean("CurseBoost");
    }

    public static void setCurseBoosted(LivingEntity entity, Boolean flag, Player player) {
        if (!OmniconfigAddonHandler.EnableCurseBoost.getValue()) return;
        if (entity instanceof Mob mob && mob.isNoAi()) return;
        LivingCurseBoostEvent event = AddonHookHandler.onLivingCurseBoosted(entity, player);
        if (!event.isCanceled())
            entity.getPersistentData().putBoolean("CurseBoost", flag);
    }


    public static @Nullable <T> LazyOptional<T> getCapability(Player player, Capability<T> capability) {
        return player == null ? null : player.getCapability(capability);
    }

    public static boolean unlockSpecialSlot(String slot, Player player) {
        if (!slot.equals("scroll")) {
            throw new IllegalArgumentException("Slot type '" + slot + "' is not supported!");
        } else {
            MutableBoolean success = new MutableBoolean(false);
            UUID id = SCROLL_SLOT_UUID;
            CuriosApi.getCuriosInventory(player).ifPresent((handler) -> handler.getStacksHandler(slot).ifPresent((stacks) -> {
                if (!stacks.getModifiers().containsKey(id)) {
                    stacks.addPermanentModifier(new AttributeModifier(id, "MasterSlot", 1.0, AttributeModifier.Operation.ADDITION));
                    success.setTrue();
                }
            }));
            return success.getValue();
        }
    }


    public static @Nullable ItemStack getChaosElytra(LivingEntity living) {
        ItemStack stack = living.getItemBySlot(EquipmentSlot.CHEST);
        return stack.is(EnigmaticAddonItems.CHAOS_ELYTRA) ? stack : SuperpositionHandler.getCurioStack(living, EnigmaticAddonItems.CHAOS_ELYTRA);
    }

    public static ItemStack getItem(Player player, Item item) {
        for (NonNullList<ItemStack> stacks : player.getInventory().compartments) {
            for (ItemStack itemstack : stacks) {
                if (!itemstack.isEmpty() && itemstack.is(item)) {
                    return itemstack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> getAllItem(Player player, Item item) {
        List<ItemStack> outputs = new ArrayList<>();
        for (NonNullList<ItemStack> stacks : player.getInventory().compartments) {
            for (ItemStack itemstack : stacks) {
                if (!itemstack.isEmpty() && itemstack.is(item)) {
                    outputs.add(itemstack);
                }
            }
        }
        return outputs;
    }

    public static DamageSource simpleSource(@NotNull Entity target, ResourceKey<DamageType> type) {
        return target.damageSources().source(type, null);
    }

    public static DamageSource damageSource(@NotNull Entity target, ResourceKey<DamageType> type, Entity source) {
        return target.damageSources().source(type, source);
    }

    public static DamageSource damageSource(@NotNull Entity target, ResourceKey<DamageType> type, Entity direct, Entity source) {
        return target.damageSources().source(type, direct, source);
    }

    private static boolean hasBlessRing(Player player) {
        return player.getPersistentData().getBoolean(BlessRing.BLESS_SPAWN) || SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.BLESS_RING);
    }

    public static boolean isTheBlessedOne(Player player) {
        return hasBlessRing(player) && !SuperpositionHandler.isTheCursedOne(player);
    }

    public static boolean isOKOne(Player player) {
        return hasBlessRing(player) || SuperpositionHandler.isTheCursedOne(player);
    }

    public static boolean isPunishedOne(LivingEntity entity) {
        return entity instanceof Player player && hasBlessRing(player) && SuperpositionHandler.isTheCursedOne(player);
    }

    public static boolean isAbyssBoost(Player player) {
        // The interface for Delicacy's Overwriting
        return false;
    }

    public static int getEnderDragonBoostLevel(EnderDragon dragon) {
        int level = 2;
        List<Player> nearbyPlayers = dragon.level().getNearbyPlayers(TargetingConditions.DEFAULT, dragon, dragon.getBoundingBox().inflate(100));
        for (Player nearbyPlayer : nearbyPlayers) {
            int heartsGained = SuperpositionHandler.getPersistentInteger(nearbyPlayer, "AbyssalHeartsGained", 0);
            level += heartsGained;
        }
        return Math.min(level, 20);
    }

    public static ItemStack findBookInBag(Player player, Item book) {
        if (!SuperpositionHandler.hasItem(player, EnigmaticAddonItems.ANTIQUE_BAG) && !player.getEnderChestInventory().hasAnyOf(Set.of(EnigmaticAddonItems.ANTIQUE_BAG)))
            return ItemStack.EMPTY;
        LazyOptional<IAntiqueBagHandler> capability = getCapability(player, AntiqueBagCapability.INVENTORY);
        if (capability != null && capability.isPresent()) {
            IAntiqueBagHandler bagHandler = capability.orElseThrow(() -> new IllegalArgumentException("Lazy optional must not be empty"));
            return bagHandler.findBook(book);
        }
        return ItemStack.EMPTY;
    }

    public static AABB getBoundingBoxAroundEntity(Entity entity, double x, double y, double z) {
        return new AABB(entity.getX() - x, entity.getY() - y, entity.getZ() - z, entity.getX() + x, entity.getY() + y, entity.getZ() + z);
    }

    public static int getRedemptionCurseAmount(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        int totalCurses = 0;
        for (Enchantment enchantment : enchantments.keySet()) {
            if (enchantment instanceof RedemptionCurseEnchantment && enchantments.get(enchantment) > 0) {
                ++totalCurses;
            }
        }
        return totalCurses;
    }

    public static @Nullable LootPoolSingletonContainer.Builder<?> createOptionalLootEntry(Item item, int weight, float minCount, float maxCount) {
        return !OmniconfigAddonHandler.isItemEnabled(item) ? null : LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(UniformGenerator.between(minCount, maxCount)));
    }

    public static @Nullable LootPoolSingletonContainer.Builder<?> createOptionalLootEntry(Item item, int weight) {
        return !OmniconfigAddonHandler.isItemEnabled(item) ? null : LootItem.lootTableItem(item).setWeight(weight);
    }

    public static List<ResourceLocation> getIceDungeons() {
        List<ResourceLocation> lootChestList = new ArrayList<>();
        lootChestList.add(BuiltInLootTables.IGLOO_CHEST);
        lootChestList.add(BuiltInLootTables.ANCIENT_CITY_ICE_BOX);
        return lootChestList;
    }

    public static List<ResourceLocation> getLeafDungeons() {
        List<ResourceLocation> lootChestList = new ArrayList<>();
        lootChestList.add(BuiltInLootTables.JUNGLE_TEMPLE);
        return lootChestList;
    }

    public static List<ResourceLocation> getEngineDungeons() {
        List<ResourceLocation> lootChestList = new ArrayList<>();
        lootChestList.add(BuiltInLootTables.SIMPLE_DUNGEON);
        lootChestList.add(BuiltInLootTables.UNDERWATER_RUIN_SMALL);
        lootChestList.add(BuiltInLootTables.UNDERWATER_RUIN_BIG);
        lootChestList.add(BuiltInLootTables.STRONGHOLD_CORRIDOR);
        lootChestList.add(BuiltInLootTables.STRONGHOLD_CROSSING);
        return lootChestList;
    }

    public static List<ResourceLocation> getSoulDungeons() {
        List<ResourceLocation> lootChestList = new ArrayList<>();
        lootChestList.add(BuiltInLootTables.BASTION_OTHER);
        lootChestList.add(BuiltInLootTables.BASTION_TREASURE);
        return lootChestList;
    }
}
